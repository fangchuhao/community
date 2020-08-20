package com.example.demo.constant;

public class CommunityConstant {
    //激活状态码
    public static final int ACTIVE_SUCCESS=1;
    public static final int ACTIVE_REPEAT=2;
    public static final int ACTIVE_FAILED=3;

    //账号超时时长
    public static final int DEFAULT_EXPIRED=3600*12;

    //记住我点击后账号超时时长
    public static final int REMEMBER_ME_EXPIRED=3600*24*30;

    // 实体类型，1代表帖子
    public static final int ENTITY_TYPE_POST=1;

    // 实体类型，2代表评论
    public static final int ENTITY_TYPE_COMMENT=2;

    // 实体类型，3代表用户
    public static final int ENTITY_TYPE_USER=3;

    // 消息状态：未读
    public static final int MESSAGE_UNREAD=0;

    // 消息状态：已读
    public static final int MESSAGE_READED=1;

    // 消息状态：已删除
    public static final int MESSAGE_DELETED=2;

    // Kafka的topic（主题）：评论
    public static final String KAFKA_TOPIC_COMMENT="comment";

    // Kafka的topic（主题）：点赞
    public static final String KAFKA_TOPIC_LIKE="like";

    // Kafka的topic（主题）：关注
    public static final String KAFKA_TOPIC_FOLLOW="follow";

    // Kafka的topic（主题）：发布帖子
    public static final String KAFKA_TOPIC_PUBLISH="publish";

    // Kafka的topic（主题）：删除帖子
    public static final String KAFKA_TOPIC_DELETE="delete";

    // Kafka的topic（主题）：分享
    public static final String KAFKA_TOPIC_SHARE="share";

    // 系统通知时使用的用户ID
    public static final int SYSTEM_USER_ID=1;

    /**
     * 权限：普通用户
     */
    public static final String AUTHORITY_USER="user";

    /**
     * 权限：版主
     */
    public static final String AUTHORITY_MODERATOR="moderator";

    /**
     * 权限：普通用户
     */
    public static final String AUTHORITY_ADMIN="admin";

    /**
     * 帖子类型：普通
     */
    public static final int DISCUSSPOST_ORDINARY=0;

    /**
     * 帖子类型：置顶
     */
    public static final int DISCUSSPOST_TOP=1;

    /**
     * 帖子状态：正常
     */
    public static final int DISCUSSPOST_NORMAL=0;

    /**
     * 帖子状态：精华
     */
    public static final int DISCUSSPOST_ESSENCE=1;

    /**
     * 帖子状态：拉黑
     */
    public static final int DISCUSSPOST_BLOCK=2;
}
