package com.example.demo;

import com.example.demo.dao.*;
import com.example.demo.entity.*;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.MailClient;
import com.example.demo.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    void userTest() {
        User user = userMapper.selectById(103);
        System.out.println(user);
        User user2 = userMapper.selectByName("guanyu");
        System.out.println(user2);
        User user3 = userMapper.selectByEmail("nowcoder13@sina.com");
        System.out.println(user3);
    }

    @Test
    void userInsertTest() {
        User user=new User();
        user.setUsername("xiaoming");
        user.setPassword("123456");
        user.setSalt("4fg13");
        user.setType(0);
        user.setHeaderUrl("http://image.io/123");
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i);
        System.out.println(user);
    }

    @Test
    void userUpdateTest() {
        int i = userMapper.updateStatus(151, 1);
        System.out.println(i);
        int i1 = userMapper.updatePassword(151, "22222");
        System.out.println(i1);
        int i2 = userMapper.updateHeader(151, "http://image.io/123123123123");
        System.out.println(i2);
    }

    @Test
    void testDiscussPostMapper() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10,0);
        for (int i = 0; i < discussPosts.size(); i++) {
            DiscussPost discussPost =  discussPosts.get(i);
            System.out.println(discussPost);
        }
        int total = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(total);
    }

    @Test
    void testMail() {
        mailClient.sendMessage("2415966685@qq.com","你好，新人！","测试一还小长假按下打算打蜡等级");
    }

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void testHtmlMail() {
        Context context=new Context();
        context.setVariable("username","oojuynj");
        String process = templateEngine.process("/mail/demo", context);
        mailClient.sendMessage("2415966685@qq.com","qwe",process);
    }

    @Test
    void testCommonUtil() {
        String str= CommonUtil.generateString();
        System.out.println("随机生成的字符串为："+str);
        str=CommonUtil.md5Encrypt(str);
        System.out.println("MD5加密后的字符串为："+str);
    }
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    void testLoginTicket() {
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(123);
        loginTicket.setTicket("131");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.addLoginTicket(loginTicket);
    }
    @Test
    public void testSensitive() {
        System.out.println(sensitiveFilter.filter("你是神***经病的----&^%$%的额的吗我去全家全是"));
    }

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void testCommentMapper() {
//        Comment comment=new Comment();
//        comment.setUserId(1);
//        comment.setContent("1312");
//        comment.setCreateTime(new Date());
//        comment.setEntityId(1);
//        comment.setEntityType(1);
//        comment.setStatus(0);
//        comment.setTargetId(0);
//        int i = commentMapper.insertComment(comment);
//        System.out.println(i);

        int i1 = discussPostMapper.updateDiscussPost(282, 1);
        System.out.println(i1);
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessageMapper() {
        messageMapper.updateMessageStatusByIds(new int[]{1,2,3,4,23,25,28,207},0);
    }


    @Test
    public void md5Encrypt() {
        String password="123";
        String salt= CommonUtil.generateString().substring(0,5);
        String activation=CommonUtil.generateString();
        String enPassword=CommonUtil.md5Encrypt(password+"167f9");
        System.out.println("salt="+salt+",activation="+activation+",password="+enPassword);
    }
}
