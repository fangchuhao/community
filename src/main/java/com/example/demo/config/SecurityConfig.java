package com.example.demo.config;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setUserInfo",
                        "/user/setheaderUrl",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        CommunityConstant.AUTHORITY_ADMIN,
                        CommunityConstant.AUTHORITY_USER,
                        CommunityConstant.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/essence"
                )
                .hasAnyAuthority(
                        CommunityConstant.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/block",
                        "/data/**",
                        "/actuator/**"
                )
                .hasAnyAuthority(
                        CommunityConstant.AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();
        // 权限不够的处理
        // 当前项目有get请求、post请求，有的希望返回的是页面，有的希望返回的是数据，不能简单地返回一个错误页面
        http.exceptionHandling()
                // 没有登录时
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 判断当前请求是同步还是异步
                        String header = request.getHeader("X-Requested-With");
                        if("XMLHttpRequest".equals(header)) {
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(CommonUtil.getJSONString(403,"请先登录系统！"));
                        }else {
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                // 已经登录，权限不够时
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String header = request.getHeader("X-Requested-With");
                        if("XMLHttpRequest".equals(header)) {
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(CommonUtil.getJSONString(403,"您没有访问当前页面的权限！"));
                        }else {
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });
        // Security底层会默认实现logout的逻辑
        // 我们需要覆盖它的逻辑，让logout执行的是我们自己定义的代码
        http.logout().logoutUrl("/logout2");
    }
}
