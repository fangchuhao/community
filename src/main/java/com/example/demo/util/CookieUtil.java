package com.example.demo.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getCookieValue(HttpServletRequest request,String key) {
        if(request==null || StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("网页请求参数错误！");
        }
        Cookie[] cookies = request.getCookies();

        if(cookies!=null) {
            for(Cookie cookie:cookies) {
                if(cookie.getName().equals(key)) {
                    return cookie.getValue().toString();
                }
            }
        }
        return null;
    }
}
