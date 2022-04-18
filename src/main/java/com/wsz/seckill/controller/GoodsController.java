package com.wsz.seckill.controller;

import com.wsz.seckill.pojo.User;
import com.wsz.seckill.service.IGoodsService;
import com.wsz.seckill.service.IUserService;
import com.wsz.seckill.utils.DateUtil;
import com.wsz.seckill.vo.DetailVo;
import com.wsz.seckill.vo.GoodsVo;
import com.wsz.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @author : wanshanzhe
 * @date : 2022/3/23 8:27 下午
 * @desc :
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转到商品页面
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        //redis中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html =  (String)valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

//        return "goodsList";
        //如果为空，手动渲染，存入Redis并返回
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;

    }

    /**
     * 跳转商品详情页
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model, User user, @PathVariable Long goodsId,
                           HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //redis中获取页面,如果不为空，直接返回页面
        String html = (String)valueOperations.get("goodsDetail:" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        LocalDateTime startDate = goodsVo.getStartDate();
        LocalDateTime endDate = goodsVo.getEndDate();
        LocalDateTime currentDate = DateUtil.dateToLocalDateTime(new Date());

        //秒杀状态
        int secKillStatus = 0;
        //秒杀倒计时
        long remainSecond = 0;
        //秒杀还未开始
        if(currentDate.isBefore(startDate)){
            remainSecond = DateUtil.getBetweenMillis(currentDate, startDate);
        }else if (currentDate.isAfter(endDate)){
        //秒杀已经结束
            secKillStatus = 2;
            remainSecond = -1;
        }else{
        //秒杀中
            secKillStatus = 1;
            remainSecond = 0;
        }

        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSecond", remainSecond);
        model.addAttribute("goods", goodsVo);
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @param goodsId
     * @return
     */
    @RequestMapping( "/toDetail2/{goodsId}")
    @ResponseBody
    public RespBean toDetail2(Model model, User user, @PathVariable Long goodsId){

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        LocalDateTime startDate = goodsVo.getStartDate();
        LocalDateTime endDate = goodsVo.getEndDate();
        LocalDateTime currentDate = DateUtil.dateToLocalDateTime(new Date());

        //秒杀状态
        int secKillStatus = 0;
        //秒杀倒计时
        long remainSecond = 0;
        //秒杀还未开始
        if(currentDate.isBefore(startDate)){
            remainSecond = DateUtil.getBetweenMillis(currentDate, startDate);
        }else if (currentDate.isAfter(endDate)){
            //秒杀已经结束
            secKillStatus = 2;
            remainSecond = -1;
        }else{
            //秒杀中
            secKillStatus = 1;
            remainSecond = 0;
        }

        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSecond);

        return RespBean.success(detailVo);
    }
}
