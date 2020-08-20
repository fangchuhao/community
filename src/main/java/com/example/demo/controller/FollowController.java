package com.example.demo.controller;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.Event;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.event.EventProducer;
import com.example.demo.service.FollowService;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follower(int entityType,int entityId) {
        User loginUser = hostHolder.getUser();
        followService.follower(loginUser.getId(),entityType,entityId);

        // 触发关注事件
        Event event=new Event().setTopic(CommunityConstant.KAFKA_TOPIC_FOLLOW)
                .setUserId(loginUser.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityAuthor(entityId);
        eventProducer.fireEvent(event);

        return CommonUtil.getJSONString(0,"已关注！");
    }


    @RequestMapping(value = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollower(int entityType,int entityId) {
        User loginUser = hostHolder.getUser();
        followService.unfollower(loginUser.getId(),entityType,entityId);
        return CommonUtil.getJSONString(0,"已取消关注！");
    }

    @RequestMapping(value = "/followee/{userId}",method = RequestMethod.GET)
    public String getFolloweeList(Model model, Page page, @PathVariable("userId") int userId) {
        User user=userService.selectById(userId);
        if(user==null) {
            throw new RuntimeException("用户不存在！");
        }
        page.setLimit(2);
        page.setUrl("/followee/"+userId);
        List<Map<String, Object>> followeeList = followService.getFolloweeList(userId, CommunityConstant.ENTITY_TYPE_USER,page.getOffet(),page.getLimit());

        if(followeeList!=null) {
            for(Map<String, Object> map:followeeList) {
                User followeeUser = (User) map.get("followeeUser");
                boolean isFan = isFan(followeeUser.getId());
                map.put("isFan",isFan);
            }
        }

        page.setTotal((int) followService.getFolloweeCount(CommunityConstant.ENTITY_TYPE_USER,userId));

        model.addAttribute("followeeList",followeeList);
        model.addAttribute("user",user);
        return "site/followee";
    }

    @RequestMapping(value = "/follower/{userId}",method = RequestMethod.GET)
    public String getFollowerList(Model model,Page page,@PathVariable("userId") int userId) {
        User user=userService.selectById(userId);
        if(user==null) {
            throw new RuntimeException("用户不存在！");
        }
        page.setLimit(2);
        page.setUrl("/follower/"+userId);
        List<Map<String, Object>> followerList = followService.getFollowerList(userId, CommunityConstant.ENTITY_TYPE_USER,page.getOffet(),page.getLimit());

        if(followerList!=null) {
            for(Map<String, Object> map:followerList) {
                User followerUser = (User) map.get("followerUser");
                boolean isFan = isFan(followerUser.getId());
                map.put("isFan",isFan);
            }
        }


        page.setTotal((int) followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId));

        model.addAttribute("followerList",followerList);
        model.addAttribute("user",user);
        return "site/follower";
    }

    public boolean isFan(int userId) {
        if(hostHolder.getUser()==null) {
            return false;
        }
        boolean isFan = followService.getFollow(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId)<0?false:true;
        return isFan;
    }
}
