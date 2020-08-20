package com.example.demo.dao;

import com.example.demo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectComments(int entityType,int entityId,int offset,int limit);

    int commentsCount(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommetById(int id);
}
