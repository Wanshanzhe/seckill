package com.wsz.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wsz.seckill.mapper.SeckillGoodsMapper;
import com.wsz.seckill.pojo.Goods;
import com.wsz.seckill.mapper.GoodsMapper;
import com.wsz.seckill.pojo.SeckillGoods;
import com.wsz.seckill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wsz.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-27
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 获取商品列表
     * @return
     */
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }

}
