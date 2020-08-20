package com.example.demo.controller;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.Comment;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Event;
import com.example.demo.event.EventProducer;
import com.example.demo.service.CommentService;
import com.example.demo.service.DiscussPostService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String add(@PathVariable("discussPostId") int discussPostId, Comment comment, Model model) {
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);

        commentService.insertComment(comment);

        // 触发评论事件
        Event event=new Event().setTopic(CommunityConstant.KAFKA_TOPIC_COMMENT).setEntityType(comment.getEntityType()).setEntityId(comment.getEntityId()).setUserId(hostHolder.getUser().getId()).setData("postId",discussPostId);

        // 获取 发布帖子或评论 的作者
        if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST) {
            DiscussPost discussPost = discussPostService.selectDisByDisId(discussPostId);
            event.setEntityAuthor(discussPost.getUserId());
        }else {
            Comment target = commentService.selectCommetById(comment.getEntityId());
            event.setEntityAuthor(target.getUserId());
        }

        eventProducer.fireEvent(event);

        if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST) {
            event=new Event()
                    .setTopic(CommunityConstant.KAFKA_TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                    .setEntityId(comment.getEntityId());
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数前存入redis缓存
        String postKey= RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postKey,discussPostId);

        return "redirect:/discuss/detail/"+discussPostId;
    }
}
