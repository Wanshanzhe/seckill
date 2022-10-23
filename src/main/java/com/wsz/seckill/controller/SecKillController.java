package com.wsz.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.wsz.seckill.exception.GlobalException;
import com.wsz.seckill.pojo.Order;
import com.wsz.seckill.pojo.SeckillMessage;
import com.wsz.seckill.pojo.SeckillOrder;
import com.wsz.seckill.pojo.User;
import com.wsz.seckill.rabbitmq.MQSender;
import com.wsz.seckill.service.IGoodsService;
import com.wsz.seckill.service.IOrderService;
import com.wsz.seckill.service.ISeckillOrderService;
import com.wsz.seckill.utils.JsonUtil;
import com.wsz.seckill.vo.GoodsVo;
import com.wsz.seckill.vo.RespBean;
import com.wsz.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : wanshanzhe
 * @date : 2022/3/29 10:36 下午
 * @desc : 秒杀
 */

@Controller
@Slf4j
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /**
     * 秒杀
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/doSecKill")
    public String doSecKill(Model model, User user, Long goodsId){
        if (user == null){
            model.addAttribute("user", user);
        }
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        //生成订单
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    /**
     * 秒杀
     * @param path
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSecKill2", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill2(@PathVariable("path") String path, User user, Long goodsId){
         if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if(!check){
             return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
         //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder )valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //根据内存标记，减少redis的访问
        if (EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long)redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId),
                Collections.EMPTY_LIST);
        if (stock < 0){
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //生成订单
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

        /**
         * 老版本（未加入消息队列秒杀）
         */
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = (SeckillOrder )redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//        if (seckillOrder != null){
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        //生成订单
//        Order order = orderService.seckill(user, goods);
//        return RespBean.success(order);
    }

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return orderId:成功，-1：秒杀失败，0：排队中
     */
    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 功能描述：获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //获取访问地址，限制访问次数，5s访问5次
        StringBuffer uri = request.getRequestURL();
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null){
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        }else if (count < 5){
            valueOperations.increment(uri + ":" + user.getId());
        }else{
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
        }
        //校验验证码
        boolean check =  orderService.checkCaptcha(captcha, user, goodsId);
        if (!check){
            return RespBean.error(RespBeanEnum.REQUEST_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping("/captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if (user == null || goodsId < 0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成算术类型验证码，存入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        redisTemplate.opsForValue().set("captcha:" + user.getId()
            + ":" +goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }

    }

    /**
     * 系统初始化执行的方法
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        //把商品库存数量加载到redis
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
