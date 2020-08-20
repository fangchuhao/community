package com.example.demo.service;

import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 对某个实体点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId,int entityType,int entityId,int beLikedUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = RedisUtil.getLikeEntityKey(entityType, entityId);
                String beLikedUserIdKey=RedisUtil.getLikeUserKey(beLikedUserId);
                Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);

                redisOperations.multi();

                if(isMember) {
                    redisTemplate.opsForValue().decrement(beLikedUserIdKey);
                    redisTemplate.opsForSet().remove(key,userId);
                }else {
                    redisTemplate.opsForValue().increment(beLikedUserIdKey);
                    redisTemplate.opsForSet().add(key,userId);
                }
                return redisOperations.exec();
            }
        });
    }

    /**
     * 获取某个实体一共的点赞数
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId) {
        String key = RedisUtil.getLikeEntityKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断某人是否对某个实体点赞
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId) {
        String key = RedisUtil.getLikeEntityKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key,userId)?1:0;
    }

    /**
     * 获取某个用户获得的所有赞
     * @param userId
     * @return
     */
    public long getUserLikeCount(int userId) {
        String likeUserKey = RedisUtil.getLikeUserKey(userId);
        Object o = redisTemplate.opsForValue().get(likeUserKey);
        if(o==null) {
            return 0;
        }
        Long count=Long.valueOf(o.toString());
        return count;
    }


}
