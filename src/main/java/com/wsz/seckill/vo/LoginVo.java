package com.wsz.seckill.vo;

import com.wsz.seckill.validator.IsMobile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author : wanshanzhe
 * @date : 2022/3/21 9:39 下午
 * 登陆对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {

    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
