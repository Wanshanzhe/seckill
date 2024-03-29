package com.wsz.seckill.config;

import com.wsz.seckill.pojo.User;
import com.wsz.seckill.service.IUserService;
import com.wsz.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : wanshanzhe
 * @date : 2022/3/26 5:12 下午
 * @desc : 用户自定义参数
 */
@Component
public class UserResolveArgument implements HandlerMethodArgumentResolver {

    @Resource
    private IUserService userService;

    /**
     * 作为条件判断，返回值为true走下面的方法
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> userClass = methodParameter.getParameterType();
        return userClass == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest requestParam = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse responseParam = webRequest.getNativeResponse(HttpServletResponse.class);
        String ticket = CookieUtil.getCookieValue(requestParam, "userTicket");
        if (StringUtils.isEmpty(ticket)){
            return new Object();
        }
        return userService.getUserByCookie(ticket, requestParam, responseParam);
    }
}
