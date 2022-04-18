package com.wsz.seckill.controller;


import com.wsz.seckill.pojo.User;
import com.wsz.seckill.service.IOrderService;
import com.wsz.seckill.vo.OrderDetailVo;
import com.wsz.seckill.vo.RespBean;
import com.wsz.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-27
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 订单详情
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail =  orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
