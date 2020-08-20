package com.example.demo.quartz;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Event;
import com.example.demo.service.DiscussPostService;
import com.example.demo.service.ElasticsearchService;
import com.example.demo.service.LikeService;
import com.example.demo.util.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostScoreRefreshJob implements Job {

    private Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static Date epoch;

    static {
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1999-03-18 00:00:00");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String key= RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(key);

        if(operations.size()==0) {
            logger.info("没有可刷新的帖子，任务结束。");
            return;
        }
        logger.info("任务开始，正在刷新"+operations.size()+"个帖子的分数。");

        while(operations.size()>0) {
            refresh((int)operations.pop());
        }

        logger.info("任务结束，刷新帖子成功。");
    }

    private void refresh(int postId) {
        DiscussPost discussPost = discussPostService.selectDisByDisId(postId);

        if(discussPost==null) {
            throw new IllegalArgumentException("帖子不存在！");
        }

        // 获取评论数量
        int commentCount = discussPost.getCommentCount();
        // 是不是精华帖
        boolean essence = discussPost.getStatus()==1;
        // 帖子获得的赞的数量
        int likeCount = (int) likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,postId);

        // 权重
        double w = (essence ? 75 : 0) + (commentCount * 10 + likeCount * 2);
        // 分数 = 权重 + 距离天数
        double score = Math.log(Math.max(1,w)) + (discussPost.getCreateTime().getTime()-epoch.getTime())/(1000*3600*24);
        // 更新帖子分数
        discussPostService.updateScore(postId,score);

        // 同步elasticsearch的数据
        discussPost.setScore(score);
        elasticsearchService.savePost(discussPost);
    }
}
