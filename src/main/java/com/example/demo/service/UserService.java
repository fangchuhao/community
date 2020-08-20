package com.example.demo.service;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.dao.LoginTicketMapper;
import com.example.demo.dao.UserMapper;
import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.User;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.MailClient;
import com.example.demo.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    //@Autowired
    //private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String path;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    public User selectById(int id) {
        User user = getUserCache(id);
        if(user==null) {
            user = initUserCache(id);
        }
        return user;
    }

    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public Map<String,String> insertUser(User user) {
        Map<String,String> map=new HashMap<>();
        if(user==null) {
            throw new IllegalArgumentException("传入参数错误！");
        }
        if(StringUtils.isEmpty(user.getUsername())) {
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isEmpty(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isEmpty(user.getEmail())) {
            map.put("emailMsg"," 邮箱不能为空！");
            return map;
        }
        User user1 = selectByName(user.getUsername());
        if(user1!=null) {
            map.put("usernameMsg","用户名已存在！");
            return map;
        }
        user1=selectByEmail(user.getEmail());
        if(user1!=null) {
            map.put("emailMsg","邮箱已被注册！");
            return map;
        }

        String salt= CommonUtil.generateString().substring(0,5);
        String activation=CommonUtil.generateString();
        String password=CommonUtil.md5Encrypt(user.getPassword()+salt);
        user.setPassword(password);
        user.setSalt(salt);
        user.setType(0);
        user.setStatus(0);
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setActivationCode(activation);
        userMapper.insertUser(user);

        Context context=new Context();
        context.setVariable("mail",user.getEmail());
        context.setVariable("url",path+"/activation/"+user.getId()+"/"+user.getActivationCode());
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMessage(user.getEmail(),"激活账号",process);

        return map;
    }

    public int activeUser(int id,String activeCode) {
        if(id<=0 || StringUtils.isEmpty(activeCode)) {
            return CommunityConstant.ACTIVE_FAILED;
        }
        User user = selectById(id);
        int status = user.getStatus();
        try {
            if(status==1) {
                return CommunityConstant.ACTIVE_REPEAT;
            }else {
                if(activeCode.equals(user.getActivationCode())) {
                    userMapper.updateStatus(id,1);
                    clearUserCache(id);
                    return CommunityConstant.ACTIVE_SUCCESS;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return CommunityConstant.ACTIVE_FAILED;
        }
        return CommunityConstant.ACTIVE_FAILED;
    }
    public Map<String,String> login(User user,int expiredTime) {
        Map<String,String> map=new HashMap<>();
        if(user==null) {
            throw new IllegalArgumentException("传入参数错误！");
        }
        if(StringUtils.isEmpty(user.getUsername())) {
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isEmpty(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user1 = selectByName(user.getUsername());
        if(user1==null) {
            map.put("usernameMsg","用户名不存在！");
            return map;
        }
        if(user1.getStatus()==0) {
            map.put("usernameMsg","该账号未激活！");
            return map;
        }

        String salt= user1.getSalt();
        String password=CommonUtil.md5Encrypt(user.getPassword()+salt);

        if(!user1.getPassword().equals(password)) {
            map.put("passwordMsg","密码错误！");
            return map;
        }

        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user1.getId());
        loginTicket.setTicket(CommonUtil.generateString());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*expiredTime));
        //loginTicketMapper.addLoginTicket(loginTicket);
        String ticketKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    public LoginTicket findLoginTicketByTicket(String ticket) {
        String ticketKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        //return loginTicketMapper.getLoginTicketByTicket(ticket);
        return loginTicket;
    }

    public int setheaderUrl(int userId,String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearUserCache(userId);
        return rows;
    }

    public int setPassword(int userId,String password){
        int rows = userMapper.updatePassword(userId,password);
        clearUserCache(userId);
        return rows;
    }


    // 1.先从redis缓存中查数据，查不到就去数据库找
    private User getUserCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(userKey);
    }

    // 2.从数据库找到后缓存到redis
    private User initUserCache(int userId) {
        User user=userMapper.selectById(userId);
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user);
        return user;
    }

    // 3.数据变更时删除缓存中的数据
    public void clearUserCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = selectById(userId);
        List<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthority() {
            public String getAuthority() {
                switch (user.getType()) {
                    case 1: return CommunityConstant.AUTHORITY_ADMIN;
                    case 2: return CommunityConstant.AUTHORITY_MODERATOR;
                    default: return CommunityConstant.AUTHORITY_USER;
                }
            }
        });
        return authorities;
    }
}
