package com.example.demo.service;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.dao.DiscussPostMapper;
import com.example.demo.entity.DiscussPost;
import com.example.demo.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;


import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private Logger logger= LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.maxSize}")
    private int maxSize;

    @Value("${caffeine.posts.expireSeconds}")
    private int expireSeconds;

    // Caffeine 核心接口: LoadingCache,AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer,Integer> postRowsCache;


    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(15)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key==null || key.length()==0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params = key.split(":");
                        if(params==null || params.length!=2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // TODO 这里可以加二级缓存,先访问redis查看有没有数据，没有再访问数据库
                        logger.debug("本地缓存无数据，帖子【列表】从数据库中读取");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("本地缓存无数据，帖子【总数】从数据库中读取");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode) {
        if(userId==0 && orderMode==1) {
            return this.postListCache.get(offset+":"+limit);
        }
        logger.debug("本地缓存无数据，帖子【列表】从数据库中读取");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int selectDiscussPostRows(int userId) {
        if(userId==0) {
            return this.postRowsCache.get(0);
        }
        logger.debug("本地缓存无数据，帖子【总数】从数据库中读取");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost) {
        if(discussPost==null) {
            throw new IllegalArgumentException("发布帖子参数错误");
        }
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost selectDisByDisId(int id) {
        return discussPostMapper.selectDisByDisId(id);
    }

    public int updateCommentCount(int id,int commentCount) {
        return discussPostMapper.updateDiscussPost(id,commentCount);
    }

    /**
     * 将帖子置顶
     */
    public int topPost(int postId) {
        return discussPostMapper.updatePostType(postId, CommunityConstant.DISCUSSPOST_TOP);
    }

    /**
     * 将帖子状态改为精华
     */
    public int essencePost(int postId) {
        return discussPostMapper.updatePostStatus(postId, CommunityConstant.DISCUSSPOST_ESSENCE);
    }

    /**
     * 将帖子状态改为拉黑
     */
    public int blockPost(int postId) {
        return discussPostMapper.updatePostStatus(postId, CommunityConstant.DISCUSSPOST_BLOCK);
    }

    /**
     * 改变帖子分数
     */
    public int updateScore(int postId,double score) {
        return discussPostMapper.updateScore(postId,score);
    }
}
