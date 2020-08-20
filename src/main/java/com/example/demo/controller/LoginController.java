package com.example.demo.controller;

import com.example.demo.config.KaptchaConfig;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.RedisUtil;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DefaultKaptcha kaptcha;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(value = "register",method = RequestMethod.GET)
    public String registry() {
        return "site/register";
    }

    @RequestMapping(value = "login",method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("kaptchaUrl","kaptcha");
        return "site/login";
    }

    @RequestMapping(value = "login",method = RequestMethod.POST)
    public String login(Model model,User user,String verifycode,boolean rememberme,@CookieValue("kaptchaOwner") String kaptchaOwner,HttpServletResponse response) {
        // 获取验证码
        //String kaptcha = (String)CommonUtil.session().getAttribute("kaptcha");
        String kaptcha=null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            kaptcha = (String) redisTemplate.opsForValue().get(RedisUtil.getKaptchaKey(kaptchaOwner));
        }

        if(StringUtils.isEmpty(kaptcha) || !kaptcha.equalsIgnoreCase(verifycode)) {
            model.addAttribute("username",user.getUsername());
            model.addAttribute("password",user.getPassword());
            model.addAttribute("verifyMsg","验证码不正确！");
            return "site/login";
        }
        int expiredTime=rememberme?CommunityConstant.REMEMBER_ME_EXPIRED:CommunityConstant.DEFAULT_EXPIRED;
        Map<String, String> login = userService.login(user,expiredTime);
        login.entrySet().stream().forEach(map->{
            model.addAttribute(map.getKey(),map.getValue());
        });
        if(login.containsKey("ticket")) {
            Cookie cookie=new Cookie("ticket",login.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredTime);
            response.addCookie(cookie);
            return "redirect:index";
        }
        return "site/login";
    }

    /**
     * 生成验证码
     */
    @RequestMapping(value = "kaptcha",method = RequestMethod.GET)
    public void kaptcha(HttpServletResponse response, HttpSession session,String n) {
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
        // session.setAttribute("kaptcha",text);

        // 将验证码存入redis
        String kaptchaOwner=CommonUtil.generateString();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);


        response.setContentType("image/png");
        OutputStream os=null;
        try {
            os=response.getOutputStream();
            ImageIO.write(image,"png",os);
            logger.info("生成验证码成功："+text);
        }catch (Exception e) {
            logger.error("生成验证码失败");
            e.printStackTrace();
        }finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "register",method = RequestMethod.POST)
    public String registry(Model model, User user) {
        try {
            Map<String, String> map = userService.insertUser(user);
            if(map == null || map.isEmpty()) {
                model.addAttribute("msg","注册成功, 请您到您的邮箱中点击激活链接来激活您的帐号");
                model.addAttribute("target","/index");
                return "site/operate-result";
            }else {
                map.entrySet().stream().forEach(entry->{
                    model.addAttribute(entry.getKey(),entry.getValue());
                });
                return "site/register";
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "site/register";
    }

    @RequestMapping(value ="activation/{userId}/{activationCode}",method = RequestMethod.GET)
    public String activeUser(Model model, @PathVariable("userId") Integer userId,@PathVariable("activationCode") String activationCode) {
        int status = userService.activeUser(userId, activationCode);
        if(status== CommunityConstant.ACTIVE_FAILED) {
            model.addAttribute("msg","激活失败");
            model.addAttribute("target","/index");
        }
        if(status== CommunityConstant.ACTIVE_SUCCESS) {
            model.addAttribute("msg","激活成功");
            model.addAttribute("target","/login");
        }
        if(status== CommunityConstant.ACTIVE_REPEAT) {
            model.addAttribute("msg","请勿重复激活");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(value = "logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:login";
    }
}
