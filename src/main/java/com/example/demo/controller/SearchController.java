package com.example.demo.controller;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.service.ElasticsearchService;
import com.example.demo.service.LikeService;
import com.example.demo.service.UserService;
import com.example.demo.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public String search(Model model,String keyword,Page page) {
        org.springframework.data.domain.Page<DiscussPost> discussPosts = elasticsearchService.searchPost(keyword, page.getCurrentPage() - 1, page.getLimit());
        List<Map<String,Object>> postMap=new ArrayList<>();
        if(discussPosts!=null) {
            for(DiscussPost post:discussPosts) {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user=userService.selectById(post.getUserId());
                map.put("user",user);
                int entityLikeCount = (int)likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",entityLikeCount);
                int status=likeService.findEntityLikeStatus(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_POST, post.getId());
                map.put("likeStatus",status);
                postMap.add(map);
            }
            page.setUrl("/search?keyword="+keyword);
            page.setTotal((int) discussPosts.getTotalElements());
        }
        model.addAttribute("postMap",postMap);
        model.addAttribute("keyword",keyword);
        return "site/search";
    }
}
