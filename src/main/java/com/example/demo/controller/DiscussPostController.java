package com.example.demo.controller;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.*;
import com.example.demo.event.EventProducer;
import com.example.demo.service.*;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 帖子控制器
 */
@RequestMapping("/discuss")
@Controller
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private EventProducer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  发布帖子/新增帖子
     * @param discussPost
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
    public String add(DiscussPost discussPost) {
        User user = hostHolder.getUser();
        if(user==null) {
            return CommonUtil.getJSONString(403,"用户未登录！");
        }

        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());

        discussPostService.insertDiscussPost(discussPost);

        Event event=new Event()
                .setTopic(CommunityConstant.KAFKA_TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        producer.fireEvent(event);

        // 计算帖子分数前存入redis缓存
        String postKey= RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postKey,discussPost.getId());


        return CommonUtil.getJSONString(0,"发布成功！");
    }
    @RequestMapping(value = "/detail/{id}",method = RequestMethod.GET)
    public String detail(@PathVariable(value = "id",required = true) Integer id, Page page, Model model) {
        User currentUser = hostHolder.getUser();
        DiscussPost discussPost = discussPostService.selectDisByDisId(id);
        // 当前帖子
        model.addAttribute("discussPost",discussPost);
        // 发布当前帖子的用户
        User user = userService.selectById(discussPost.getUserId());
        model.addAttribute("user",user);
        long tieziLikeCount=likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
        // 当前帖子点赞数量
        model.addAttribute("tieziLikeCount",tieziLikeCount);
        // 用户对当前帖子点赞状态
        int tieziLikeStatus=currentUser==null?0:likeService.findEntityLikeStatus(currentUser.getId(),CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
        model.addAttribute("tieziLikeStatus",tieziLikeStatus);

        // 查询当前帖子的评论
        List<Comment> comments = commentService.selectComments(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId(), page.getOffet(), page.getLimit());
        int commentsCount=commentService.commentsCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
        List<Map<String,Object>> commentsMap=new ArrayList<>();
        for(Comment comment:comments) {
            Map<String,Object> map=new HashMap<>();
            User commentUser=userService.selectById(comment.getUserId());
            map.put("commentUser",commentUser);
            map.put("comment",comment);
            // 回复帖子的评论的点赞数量 和 用户对其点赞状态
            long huitieLikeCount=likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
            map.put("huitieLikeCount",huitieLikeCount);
            int huitieLikeStatus=currentUser==null?0:likeService.findEntityLikeStatus(currentUser.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
            map.put("huitieLikeStatus",huitieLikeStatus);
            commentsMap.add(map);

            // 获取评论的评论
            List<Comment> replys=commentService.selectComments(CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
            // 每条评论底下回复的数量
            int replysCount=commentService.commentsCount(CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
            map.put("replysCount",replysCount);
            List<Map<String,Object>> replyMap=new ArrayList<>();
            for(Comment reply:replys) {
                Map<String,Object> map2=new HashMap<>();
                map2.put("reply",reply);
                // 作者
                User author=userService.selectById(reply.getUserId());
                map2.put("user",author);
                // 目标
                map2.put("target",null);
                if(reply.getTargetId()!=0) {
                    User target=userService.selectById(reply.getTargetId());
                    if(target!=null) {
                        map2.put("target",target);
                    }
                }
                // 回复评论的评论的点赞数量 和 用户对其点赞状态
                long replyLikeCount=likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId());
                map2.put("replyLikeCount",replyLikeCount);
                int replyLikeStatus=currentUser==null?0:likeService.findEntityLikeStatus(currentUser.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId());
                map2.put("replyLikeStatus",replyLikeStatus);
                replyMap.add(map2);
            }
            map.put("replyMap",replyMap);
        }
        model.addAttribute("commentsMap",commentsMap);
        model.addAttribute("commentsCount",commentsCount);
        page.setTotal(commentsCount);
        page.setUrl("/discuss/detail/"+id);
        return "site/discuss-detail";
    }

    /**
     * 置顶请求
     */
    @RequestMapping(value = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String top(int id) {
        discussPostService.topPost(id);
        Event event=new Event()
                .setTopic(CommunityConstant.KAFKA_TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        producer.fireEvent(event);

        return CommonUtil.getJSONString(0);
    }

    /**
     * 加精请求
     */
    @RequestMapping(value = "/essence",method = RequestMethod.POST)
    @ResponseBody
    public String essence(int id) {
        discussPostService.essencePost(id);
        Event event=new Event()
                .setTopic(CommunityConstant.KAFKA_TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        producer.fireEvent(event);

        // 计算帖子分数前存入redis缓存
        String postKey= RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postKey,id);

        return CommonUtil.getJSONString(0);
    }

    /**
     * 拉黑请求
     */
    @RequestMapping(value = "/block",method = RequestMethod.POST)
    @ResponseBody
    public String block(int id) {
        discussPostService.blockPost(id);
        Event event=new Event()
                .setTopic(CommunityConstant.KAFKA_TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        producer.fireEvent(event);

        return CommonUtil.getJSONString(0);
    }
}
