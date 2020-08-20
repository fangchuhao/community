package com.example.demo;

import com.example.demo.entity.DiscussPost;
import com.example.demo.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@org.springframework.boot.test.context.SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class SpringBootTest {

    @Autowired
    private DiscussPostService postService;

    private DiscussPost data=new DiscussPost();

    @BeforeClass
    public static void beforeClass() {
        System.out.println("before class");
    }

    @Before
    public void before() {
        System.out.println("before");
        data.setTitle("斯柯达SDK啦");
        data.setContent("打算看懂我去拿下");
        data.setCreateTime(new Date());
        data.setUserId(111);
        data.setScore(Math.random()*2000);
        postService.insertDiscussPost(data);
    }

    @After
    public void after() {
        System.out.println("after");
        postService.blockPost(data.getId());
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("after class");
    }

    @Test
    public void test1() {
        DiscussPost discussPost = postService.selectDisByDisId(data.getId());
        Assert.assertNotNull(discussPost);
        Assert.assertEquals(data.getTitle(),discussPost.getTitle());
        Assert.assertEquals(data.getContent(),discussPost.getContent());
    }

    @Test
    public void test2() {
        int rows = postService.updateScore(data.getId(),12000);
        Assert.assertEquals(rows,1);

        DiscussPost discussPost = postService.selectDisByDisId(data.getId());
        Assert.assertEquals(12000.00,discussPost.getScore(),2);
    }

}
