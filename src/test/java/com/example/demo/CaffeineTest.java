package com.example.demo;

import com.example.demo.entity.DiscussPost;
import com.example.demo.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class CaffeineTest {
    @Autowired
    private DiscussPostService postService;

    @Test
    public void test1() {
        for(int i=0;i<300000;i++) {
            DiscussPost discussPost=new DiscussPost();
            discussPost.setTitle("miniso8.18活动");
            discussPost.setContent("昨天看了我初中喜欢的人在空间发的一篇关于怀念初中的短篇小说吧，记录了他对一些记忆深刻的人的评价，而我也看到了我明明一直清楚，却不肯打心底接受的真相，他喜欢另外一个人，过往那么多的蛛丝马迹，那么多明明一件事就能看出他喜欢的人是她的真相，可我却是宁愿找借口自欺欺人，而今他给出坦白答案，他是放下了，才说出来，我是不是也该放下了，直到今日才明白我一个人自以为刻骨铭心的回忆，他也许早就忘怀，他的短篇小说故事中我没有丝言片语，也许若干年后他回想起来的只是我的名字，我只是个戏子，在他的故事中流着自己的泪，一个于他青春年华中不曾使他掀起过一丝波澜的模糊影子，而他不知道也永远不会知道，我的故事里他出现的很多，占了很多篇幅，我把他写进我的故事，因为他路过我心上，他踏着万千星河而来，又乘舟奔赴远方，我与春风皆过客，你携秋水揽星河。如今看来万般故事不过情伤，易水人去，明月如霜。");
            discussPost.setCreateTime(new Date());
            discussPost.setUserId(111);
            discussPost.setScore(Math.random()*2000);
            postService.insertDiscussPost(discussPost);
        }
    }

    @Test
    public void test2() {
        postService.selectDiscussPosts(0,0,10,1);
        postService.selectDiscussPosts(0,0,10,1);
        postService.selectDiscussPosts(0,0,10,1);
        postService.selectDiscussPosts(0,0,10,0);
    }
}
