package com.wsz.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author : wanshanzhe
 * @date : 2022/3/21 9:20 下午
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    /**
     * 通用枚举
     */
    SUCCESS(200, "SUCCESS"),
    ERROR(200, "服务端异常"),

    /**
     * 登录模块
     */
    LOGIN_ERROR(500210, "用户名或密码错误"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    MOBILE_NOT_EXIST(500212, "手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500213, "密码更新失败"),
    SESSION_ERROR(500214, "用户不存在"),

    /**
     * 异常模块
     */
    BIND_ERROR(500212, "参数校验异常"),

    /**
     * 秒杀模块
     */
    EMPTY_STOCK(500500, "库存不足"),
    REPEATE_ERROR(500501, "该商品每人限购一件"),
    REQUEST_ILLEGAL(500502, "请求非法，请重新重试"),
    REQUEST_CAPTCHA(500503, "验证码错误，请重新输入"),
    ACCESS_LIMIT_REAHCED(500504, "访问过于频繁，请稍后再试"),

    /**
     * 订单模块
     */
    ORDER_NOT_EXIST(500300, "订单信息不存在");

    private final Integer code;
    private final String message;

}
