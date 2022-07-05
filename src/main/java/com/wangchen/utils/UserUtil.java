package com.wangchen.utils;


import com.wangchen.entity.SysUser;
import com.wangchen.service.SysUserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Random;

public class UserUtil {

    public static SysUser checkManager(SysUserService managerService, HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return null;
        for (Cookie cookie : cookies){
            if ("shopToken".equals(cookie.getName())){
                String token = cookie.getValue();
                return managerService.selectByToken(token);
            }
        }
        return null;
    }
    public static String getRandom(int length){
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }
}
