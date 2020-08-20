package com.example.demo.dao;

import com.example.demo.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /**
     * 查询用户发帖数，若传入userId=0，则表示查询所有用户发帖
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId")int userId, @Param("offset")int offset, @Param("limit")int limit,@Param("orderMode")int orderMode);

    /**
     * 查询用户发帖总数量，若传入userId=0，则表示查询所有用户发帖数量
     * @param userId
     * @return
     */
    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    DiscussPost selectDisByDisId(@Param("id")int id);

    int insertDiscussPost(DiscussPost discussPost);

    int updateDiscussPost(int id,int commentCount);

    /**
     * 修改帖子状态
     */
    int updatePostStatus(int id,int status);

    /**
     * 修改帖子类型
     */
    int updatePostType(int id,int type);

    /**
     * 修改帖子分数
     */
    int updateScore(int id,double score);
}
