package com.example.demo.controller;

import com.example.demo.annotation.LoginRequest;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private FollowService followService;

    /**
     * 网站首页
     * @param model
     * @param page
     * @return
     */
    @RequestMapping("/index")
    public String index(Model model, Page page, @RequestParam(value = "orderMode",defaultValue = "0")Integer orderMode) {
        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(0, page.getOffet(), page.getLimit(),orderMode);
        page.setTotal(discussPostService.selectDiscussPostRows(0));
        page.setUrl("/index?orderMode="+orderMode);
        List<Map<String,Object>> discussPostMap=new ArrayList<>();
        long likeCount=0;
        for(DiscussPost discussPost: discussPosts) {
            Map<String,Object> map=new HashMap<>();
            map.put("post",discussPost);
            User user = userService.selectById(discussPost.getUserId());
            map.put("user",user);
            likeCount=likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
            map.put("likeCount",likeCount);
            discussPostMap.add(map);
        }
        System.out.println(page+"==="+page.getFrom()+"==="+page.getTo()+"==="+page.getTotalPage());
        model.addAttribute("discussPostMap",discussPostMap);
        model.addAttribute("orderMode",orderMode);
        return "index";
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String toError() {
        return "error/500";
    }

    /**
     * 个人主页
     * @param userId
     * @return
     */
    // @LoginRequest
    @RequestMapping("/profile/{userId}")
    public String profile(Model model,@PathVariable("userId") int userId) {
        // 查询该用户的个人信息
        User user=userService.selectById(userId);
        if(user==null) {
            throw new RuntimeException("用户不存在！");
        }

        long userLikeCount = likeService.getUserLikeCount(userId);
        model.addAttribute("user",user);
        model.addAttribute("userLikeCount",userLikeCount);

        // 当前登录用户
        User loginUser = hostHolder.getUser();
        model.addAttribute("loginUser",loginUser);


        // 判断登录用户是否关注当前用户——是否关注
        int isFollow = loginUser==null?-1:followService.getFollow(loginUser.getId(),CommunityConstant.ENTITY_TYPE_USER,user.getId());
        model.addAttribute("isFollow",isFollow);
        // 获取当前详注情页面用户的关注者数量（有多少人关他）——粉丝数量
        long followerCount = followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount",followerCount);
        // 获取当前详情页面用户关注数量（他关注了多少人）——关注数量
        long followeeCount = followService.getFolloweeCount(CommunityConstant.ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followeeCount",followeeCount);


        return "site/profile";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDenied() {
        return "/error/404";
    }
}
