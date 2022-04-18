package com.wsz.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wanshanzhe
 * @date : 2022/4/17 9:23 上午
 * @desc : 秒杀信息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {

    private User user;

    private Long goodId;
}
