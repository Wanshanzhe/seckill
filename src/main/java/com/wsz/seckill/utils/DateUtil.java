package com.wsz.seckill.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author : wanshanzhe
 * @date : 2022/3/28 10:19 下午
 * @desc : 时间工具类
 */
public class DateUtil {

    /**
     * date 转 LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    /**
     * 计算两个时间值的秒差
     * @param front
     * @param rear
     * @return
     */
    public static long getBetweenMillis(LocalDateTime front, LocalDateTime rear){
        return ChronoUnit.SECONDS.between(front, rear);
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = dateToLocalDateTime(new Date());
        System.out.println(localDateTime);

    }
}
