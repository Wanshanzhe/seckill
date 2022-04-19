package com.wsz.seckill.config;

import com.wsz.seckill.pojo.User;

/**
 * @author : wanshanzhe
 * @date : 2022/4/19 10:23 下午
 * @desc :
 */
public class UserContext {

    private static ThreadLocal<User> userHolder = new ThreadLocal<User>();

    public static void setUse(User user){
        userHolder.set(user);
    }

    public static User getUser(){
        return userHolder.get();
    }
}
