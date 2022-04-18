package com.wsz.seckill.service;

import com.wsz.seckill.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wsz.seckill.vo.LoginVo;
import com.wsz.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-20
 */
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginVo
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据用户cook获取用户
     * @param userTicket
     * @return
     */
    User  getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);


    /**
     * 更新密码
     * @param userTicket
     * @param id
     * @param password
     * @param request
     * @param response
     * @return
     */
    RespBean updatePassword(String userTicket, Long id, String password, HttpServletRequest request, HttpServletResponse response);
}
