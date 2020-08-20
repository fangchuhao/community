package com.example.demo.config;

import com.example.demo.interceptor.DataInterceptor;
import com.example.demo.interceptor.LoginInterceptor;
import com.example.demo.interceptor.LoginRequestInterceptor;
import com.example.demo.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
    @Autowired
    private LoginInterceptor loginInterceptor;
//    @Autowired
//    private LoginRequestInterceptor loginRequestInterceptor;
    @Autowired
    private MessageInterceptor messageInterceptor;
    @Autowired
    private DataInterceptor dataInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .excludePathPatterns("/**/*.css","/**/*.js","/img/**");

//        registry.addInterceptor(loginRequestInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/img/**");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/img/**");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/img/**");
    }
}
