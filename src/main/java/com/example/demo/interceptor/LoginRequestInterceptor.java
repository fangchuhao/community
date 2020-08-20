package com.example.demo.interceptor;

import com.example.demo.annotation.LoginRequest;
import com.example.demo.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequestInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    private Logger logger= LoggerFactory.getLogger(LoginRequestInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod=(HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            LoginRequest loginRequest = method.getAnnotation(LoginRequest.class);
            if(loginRequest != null && hostHolder.getUser()==null) {
                logger.info("请求需要登录，即将重定向到登录页面:"+request.getContextPath()+"/login");
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }

        return true;
    }
}
