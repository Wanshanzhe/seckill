package com.wsz.seckill.vo;

import com.wsz.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wanshanzhe
 * @date : 2022/4/7 9:29 下午
 * @desc : 详情返回对象
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailVo {

    private User user;

    private GoodsVo goodsVo;

    private int secKillStatus;

    private long remainSeconds;
}
