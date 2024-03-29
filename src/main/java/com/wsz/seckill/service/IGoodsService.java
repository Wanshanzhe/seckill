package com.wsz.seckill.service;

import com.wsz.seckill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wsz.seckill.pojo.SeckillGoods;
import com.wsz.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-27
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
