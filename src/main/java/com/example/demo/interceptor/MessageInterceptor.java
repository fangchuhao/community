package com.example.demo.interceptor;

import com.example.demo.entity.User;
import com.example.demo.service.MessageService;
import com.example.demo.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor{
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 查询未读消息数量
        User loginUser = hostHolder.getUser();
        if(loginUser!=null && modelAndView!=null) {
            int unReadMessageTotal = messageService.countUnReadMessage(loginUser.getId(),null);
            int totalUnReadNotice = messageService.countUnReadedByKafkaType(loginUser.getId(),null);
            int totalMessage=unReadMessageTotal+totalUnReadNotice;
            modelAndView.addObject("totalMessage",totalMessage);
        }
    }
}
