package com.wsz.seckill.utils;

import java.util.UUID;

/**
 * @author : wanshanzhe
 * @date : 2022/3/23 8:10 下午
 * @desc : UUID工具类
 */
public class UUIDUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
