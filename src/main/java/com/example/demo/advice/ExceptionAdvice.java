package com.example.demo.advice;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice(annotations = {Controller.class})
public class ExceptionAdvice {
    private Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e,HttpServletRequest request,HttpServletResponse response) {
        logger.error("服务器端报错："+e.getMessage());
        StackTraceElement[] stackTraces = e.getStackTrace();
        for(StackTraceElement stackTrace:stackTraces) {
            logger.error(stackTrace.toString());
        }
        try{
            if("XMLHttpRequest".equals(request.getHeader("x-requested-with"))) {
                response.setContentType("application/plain;charset=utf-8");
                    response.getWriter().write(CommonUtil.getJSONString(1,"服务器异常"));
            }else {
                response.sendRedirect(request.getContextPath()+"/error");
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }

    }
}
