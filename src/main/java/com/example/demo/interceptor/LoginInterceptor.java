package com.example.demo.interceptor;

import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.CookieUtil;
import com.example.demo.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginInterceptor implements HandlerInterceptor{
    private Logger log= LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getCookieValue(request, "ticket");
        LoginTicket loginTicketByTicket=null;
        if(ticket==null) {
            return true;
        }
        loginTicketByTicket = userService.findLoginTicketByTicket(ticket);
        if(loginTicketByTicket==null || loginTicketByTicket.getStatus()==1 || loginTicketByTicket.getExpired().after(new Date())) {
            return true;
        }
        User user = userService.selectById(loginTicketByTicket.getUserId());
        if(user==null) {
            return true;
        }
        hostHolder.setUser(user);
        // 构建用户认证的结果，并存入SecurityContext,以便于Security进行授权
        Authentication authentication=new UsernamePasswordAuthenticationToken(user,user.getPassword(),userService.getAuthorities(user.getId()));
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        log.info("用户【"+user.getUsername()+"】登录成功，成功保存用户信息，接下来将渲染到视图模板上");
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user=hostHolder.getUser();
        if(user!=null && modelAndView!=null) {
            modelAndView.addObject("loginUser",user);
            log.info("已经将用户【"+user.getUsername()+"】渲染到视图上，即将跳转到页面");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.remove();
    }
}
