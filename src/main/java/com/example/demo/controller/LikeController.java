package com.example.demo.controller;

import com.example.demo.constant.CommunityConstant;

import com.example.demo.entity.Event;
import com.example.demo.entity.User;
import com.example.demo.event.EventProducer;

import com.example.demo.service.LikeService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int beLikedUserId,int postId) {
        User user=hostHolder.getUser();
        likeService.like(user.getId(),entityType,entityId,beLikedUserId);
        long likeCount=likeService.findEntityLikeCount(entityType,entityId);
        int status=likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("status",status);

        if(status==1) {
            // 触发点赞事件
            Event event=new Event().setTopic(CommunityConstant.KAFKA_TOPIC_LIKE).setEntityType(entityType).setEntityId(entityId).setUserId(user.getId()).setEntityAuthor(beLikedUserId).setData("postId",postId);

            eventProducer.fireEvent(event);
        }

        if(entityType==CommunityConstant.ENTITY_TYPE_POST) {
            // 计算帖子分数前存入redis缓存
            String postKey= RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postKey,entityId);
        }

        return CommonUtil.getJSONString(0,null,map);
    }
}
