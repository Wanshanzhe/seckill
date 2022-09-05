package com.wsz.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * @author : wanshanzhe
 * @date : 2022/3/20 5:24 下午
 * MD5工具类
 */

@Component
public class MD5Util {

    public static String password_md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    private static final String PASSWORD_SALT = "3c3d3e3f3g";

    /**
     * 第一次加密（前端展示的密码）
     *
     * @param inputPass
     * @return
     */
    public static String convertToFromPass(String inputPass) {
        String str = "" + PASSWORD_SALT.charAt(0) + PASSWORD_SALT.charAt(1) + inputPass + PASSWORD_SALT.charAt(2) + PASSWORD_SALT.charAt(3);
        return password_md5(str);
    }

    /**
     * 二次加密 后端存入数据库的密码
     *
     * @param formPass
     * @param PASSWORD_SALT
     * @return
     */
    public static String covertPassToDBPass(String formPass, String PASSWORD_SALT) {
        String str = "" + PASSWORD_SALT.charAt(0) + PASSWORD_SALT.charAt(1) + formPass + PASSWORD_SALT.charAt(2) + PASSWORD_SALT.charAt(3);
        return password_md5(str);
    }

    /**
     * 二次加密之后的数据库入库
     *
     * @param inputPass
     * @param PASSWORD_SALTDB
     * @return
     */
    public static String inputPassToDbPass(String inputPass, String PASSWORD_SALTDB) {
        String formPass = convertToFromPass(inputPass);
        String dbPass = covertPassToDBPass(formPass, PASSWORD_SALTDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(convertToFromPass("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(covertPassToDBPass(convertToFromPass("123456"), "3c3d3e3f3g"));
        System.out.println(inputPassToDbPass("123456", "3c3d3e3f3g"));
    }
}
