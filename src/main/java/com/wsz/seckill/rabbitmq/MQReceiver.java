package com.wsz.seckill.rabbitmq;

import com.wsz.seckill.pojo.SeckillMessage;
import com.wsz.seckill.pojo.SeckillOrder;
import com.wsz.seckill.pojo.User;
import com.wsz.seckill.service.IGoodsService;
import com.wsz.seckill.service.IOrderService;
import com.wsz.seckill.utils.JsonUtil;
import com.wsz.seckill.vo.GoodsVo;
import com.wsz.seckill.vo.RespBean;
import com.wsz.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author : wanshanzhe
 * @date : 2022/4/10 5:21 下午
 * @desc : 消息消费者
 */

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderService orderService;

    /**
     * 下单操作
     * @param message
     */
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVO = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVO.getStockCount() < 1){
            return;
        }

        //判断是否重复抢购
        String seckillOrderJson = (String) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(seckillOrderJson)) {
            return;
        }

        //下单操作
        orderService.seckill(user, goodsVO);
    }
}
