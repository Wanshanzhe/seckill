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
 * @date : 2022/3/29 10:36 δΈε
 * @desc : η§ζ
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
     * η§ζ
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/doSecKill")
    public String doSecKill(Model model, User user, Long goodsId){
        if (user == null){

        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //ε€ζ­εΊε­
        if (goods.getStockCount() < 1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //ε€ζ­ζ―ε¦ιε€ζ’θ΄­
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        //ηζθ?’ε
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    /**
     * η§ζ
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
         //ε€ζ­ζ―ε¦ιε€ζ’θ΄­
        SeckillOrder seckillOrder = (SeckillOrder )valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //ζ Ήζ?εε­ζ θ?°οΌεε°redisηθ?Ώι?
        if (EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //ι’εεΊε­
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long)redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId),
                Collections.EMPTY_LIST);
        if (stock < 0){
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //ηζθ?’ε
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

        /**
         * θηζ¬οΌζͺε ε₯ζΆζ―ιεη§ζοΌ
         */
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //ε€ζ­εΊε­
//        if (goods.getStockCount() < 1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //ε€ζ­ζ―ε¦ιε€ζ’θ΄­
//        SeckillOrder seckillOrder = (SeckillOrder )redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//        if (seckillOrder != null){
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        //ηζθ?’ε
//        Order order = orderService.seckill(user, goods);
//        return RespBean.success(order);
    }

    /**
     * θ·εη§ζη»ζ
     * @param user
     * @param goodsId
     * @return orderId:ζεοΌ-1οΌη§ζε€±θ΄₯οΌ0οΌζιδΈ­
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
     * εθ½ζθΏ°οΌθ·εη§ζε°ε
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
        //θ·εθ?Ώι?ε°εοΌιεΆθ?Ώι?ζ¬‘ζ°οΌ5sθ?Ώι?5ζ¬‘
        StringBuffer uri = request.getRequestURL();
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null){
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        }else if (count < 5){
            valueOperations.increment(uri + ":" + user.getId());
        }else{
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
        }
        //ζ ‘ιͺιͺθ―η 
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
        // θ?Ύη½?θ―·ζ±ε€΄δΈΊθΎεΊεΎηη±»ε
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // ηζη?ζ―η±»ειͺθ―η οΌε­ε₯redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        redisTemplate.opsForValue().set("captcha:" + user.getId()
            + ":" +goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("ιͺθ―η ηζε€±θ΄₯", e.getMessage());
        }

    }

    /**
     * η³»η»εε§εζ§θ‘ηζΉζ³
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        //ζεεεΊε­ζ°ιε θ½½ε°redis
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
