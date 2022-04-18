package com.wsz.seckill.service;

import com.wsz.seckill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wsz.seckill.pojo.User;
import com.wsz.seckill.vo.GoodsVo;
import com.wsz.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-27
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    Order seckill(User user, GoodsVo goods);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(User user, Long goodsId);

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    boolean checkPath(User user, Long goodsId, String path);

    /**
     * 校验验证码
     * @param captcha
     * @return
     */
    boolean checkCaptcha(String captcha, User user, Long goodsId);


}
