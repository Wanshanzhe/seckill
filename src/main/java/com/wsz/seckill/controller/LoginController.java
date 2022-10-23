package com.wsz.seckill.controller;

import com.wsz.seckill.service.IUserService;
import com.wsz.seckill.utils.CookieUtil;
import com.wsz.seckill.vo.LoginVo;
import com.wsz.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author : wanshanzhe
 * @date : 2022/3/20 12:58 下午
 */

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 跳转登陆页面
     *
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    /**
     * 登陆功能
     *
     * @param loginVo
     * @return
     */
    @RequestMapping(value = "/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        return userService.doLogin(loginVo, request, response);
    }

    @RequestMapping(value = "/logout")
    @ResponseBody
    public RespBean logout(HttpServletRequest request, HttpServletResponse response) {

        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        //1.清除redis缓存
        if (StringUtils.isNotBlank(userTicket)){
            redisTemplate.delete("user:" + userTicket);
        }
        //清除浏览器缓存
        CookieUtil.deleteCookie(request, response, "userTicket");
        return RespBean.success();
    }

}
