package com.wsz.seckill.vo;

import com.wsz.seckill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wanshanzhe
 * @date : 2022/4/10 10:34 上午
 * @desc : 订单详情返回对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {

    private Order order;

    private GoodsVo goodsVo;

}
