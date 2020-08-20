package com.example.demo.util;

/**
 * redis工具类：生成key给程序复用
 */
public class RedisUtil {
    // key的连接符
    private static final String SPLITKEY=":";
    // 点赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    // 用户收到的点赞前缀
    private static final String PREFIX_USER_LIKE="like:user";
    // 关注用户（点击关注的用户）
    private static final String PREFIX_FOLLOWEE="followee";
    // 被关注人
    private static final String PREFIX_FOLLOWER="follower";
    // 验证码
    private static final String PREFIX_KAPTCHA="kaptcha";
    // ticket
    private static final String PREFIX_TICKET="ticket";
    // 用户
    private static final String PREFIX_USER="user";
    // 网站访问量
    private static final String PREFIX_UV="uv";
    // 活跃用户
    private static final String PREFIX_DAU="dau";

    private static final String PREFIX_POST="post";


    // 某个实体的赞
    public static String getLikeEntityKey(int entityType,int entityId) {
        // like:entity:1:111
        String key=PREFIX_ENTITY_LIKE+SPLITKEY+entityType+SPLITKEY+entityId;
        return key;
    }

    // 用user作为key，用于存储某个用户收到的赞
    public static String getLikeUserKey(int userId) {
        String key=PREFIX_USER_LIKE+SPLITKEY+userId;
        return key;
    }

    // 某个用户关注的实体
    // 产生用户关注的key
    // followee:userId:entityType -> zset(entityId,now)
    // 意思就是 userId 在 now（时间） 关注了 类型为 entityType 的 entityId
    // 便于统计用户 关注 帖子/用户/其他实体 的数量
    public static String getFolloweeKey(int userId,int entityType) {
        return PREFIX_FOLLOWEE+SPLITKEY+userId+SPLITKEY+entityType;
    }


    // 用户的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId) {
        return PREFIX_FOLLOWER+SPLITKEY+entityType+SPLITKEY+entityId;
    }

    /**
     * 生成验证码的key
     * @param owner
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA+SPLITKEY+owner;
    }

    /**
     * 生成ticket的key
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET+SPLITKEY+ticket;
    }

    /**
     * 生成ticket的key
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER+SPLITKEY+userId;
    }

    /**
     * 单日访问量
     */
    public static String getUVKey(String date) {
        return PREFIX_UV+SPLITKEY+date;
    }

    /**
     * 区间访问量
     */
    public static String getUVKey(String startDate,String endDate) {
        return PREFIX_UV+SPLITKEY+startDate+SPLITKEY+endDate;
    }

    /**
     * 单日活跃数
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU+SPLITKEY+date;
    }


    /**
     * 区间活跃数
     */
    public static String getDAUKey(String startDate,String endDate) {
        return PREFIX_DAU+SPLITKEY+startDate+SPLITKEY+endDate;
    }

    /**
     * 帖子分数
     */
    public static String getPostScoreKey() {
        return PREFIX_POST+SPLITKEY+"score";
    }
}
