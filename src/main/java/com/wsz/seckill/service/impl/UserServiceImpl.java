package com.wsz.seckill.service.impl;

import com.wsz.seckill.exception.GlobalException;
import com.wsz.seckill.pojo.User;
import com.wsz.seckill.mapper.UserMapper;
import com.wsz.seckill.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wsz.seckill.utils.CookieUtil;
import com.wsz.seckill.utils.MD5Util;
import com.wsz.seckill.utils.UUIDUtil;
import com.wsz.seckill.vo.LoginVo;
import com.wsz.seckill.vo.RespBean;
import com.wsz.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wanshanzhe
 * @since 2022-03-20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户登录
     *
     * @param loginVo
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //校验密码是否正确
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生产cookie
        String ticket = UUIDUtil.uuid();
        /**
         * 把用户信息放入session中（不推荐使用）
         */
//        request.getSession().setAttribute(ticket, user);
        /**
         * 将用户信息存入redis中
         */
        redisTemplate.opsForValue().set("user:" + ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        return RespBean.success(user);
    }

    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User)redisTemplate.opsForValue().get("user:" + userTicket);
        if (user != null){
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    /**
     * 更新密码
     * @param userTicket
     * @param id
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket, Long id, String password, HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if (user == null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDbPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if (1 == result){
            //删除Redis
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }


}
