package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follower(int userId,int entityType,int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityType,entityId);
                redisOperations.multi();
                redisTemplate.opsForZSet().add(followeeKey,entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollower(int userId,int entityType,int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityType,entityId);
                redisOperations.multi();
                redisTemplate.opsForZSet().remove(followeeKey,entityId);
                redisTemplate.opsForZSet().remove(followerKey,userId);

                return redisOperations.exec();
            }
        });
    }

    // 查询某个用户是否关注了某个实体
    public int getFollow(int userId,int entityType,int entityId) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        int isFollow= redisTemplate.opsForZSet().rank(followeeKey,entityId)==null?-1:1;
        return isFollow;
    }

    // 查询某个用户粉丝的数量
    public long getFollowerCount(int entityType,int entityId) {
        String followerKey = RedisUtil.getFollowerKey(entityType, entityId);
        Long count = redisTemplate.opsForZSet().zCard(followerKey);
        return count;
    }

    // 查询某个用户关注实体的数量
    public long getFolloweeCount(int entityType,int userId) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        Long count = redisTemplate.opsForZSet().zCard(followeeKey);
        return count;
    }

    // 查询某个用户的关注列表
    public List<Map<String,Object>> getFolloweeList(int userId, int entityType,int offset,int limit) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);

        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(followeeKey, 0, System.currentTimeMillis(),offset,limit);

        if(set==null) {
            return null;
        }

        List<Map<String,Object>> followeeUsers=new ArrayList<>();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map<String,Object> map=new HashMap<>();
            ZSetOperations.TypedTuple<Object> typedTuple = (ZSetOperations.TypedTuple<Object>) iterator.next();
            int value = (int) typedTuple.getValue();
            double score = typedTuple.getScore();
            User followeeUser = userService.selectById(value);
            map.put("followeeUser",followeeUser);
            // 这里也可以选择传new Date()对象然后前端格式化日期
            String followeeTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((long) score)).toString();
            map.put("followeeTime",followeeTime);
            String followerKey = RedisUtil.getFollowerKey(entityType,value);

            followeeUsers.add(map);
        }
        return followeeUsers;
    }


    // 查询某个用户的粉丝列表
    public List<Map<String,Object>> getFollowerList(int userId, int entityType,int offset,int limit) {
        String followerKey = RedisUtil.getFollowerKey(entityType, userId);

        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(followerKey, 0, System.currentTimeMillis(),offset,limit);

        if(set==null) {
            return null;
        }

        List<Map<String,Object>> followerUsers=new ArrayList<>();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map<String,Object> map=new HashMap<>();
            ZSetOperations.TypedTuple<Object> typedTuple = (ZSetOperations.TypedTuple<Object>) iterator.next();
            int value = (int) typedTuple.getValue();
            double score = typedTuple.getScore();
            User followerUser = userService.selectById(value);
            map.put("followerUser",followerUser);
            String followerTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((long) score)).toString();
            map.put("followerTime",followerTime);
            String followerKey2 = RedisUtil.getFollowerKey(entityType,value);

            followerUsers.add(map);
        }
        return followerUsers;
    }

}
