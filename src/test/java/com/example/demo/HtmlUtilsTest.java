package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.HtmlUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class HtmlUtilsTest {
    String html = "<ul class=\"nav\"><li><a href=\"http://www.mkfree.com\">首 页</a></li>"+"<li class=\"active\"><a href=\"http://blog.mkfree.com\">博客</a></li>"+"<li><a href=\"#\">RSS</a></li></ul>";

    @Test
    public void testHtmlEscape() {
        String s = HtmlUtils.htmlEscape(html);
        System.out.println(s);
    }
    @Test
    public void testHtmlEscapeDecimal() {
        String s = HtmlUtils.htmlEscapeDecimal(html);
        System.out.println(s);
    }

}
