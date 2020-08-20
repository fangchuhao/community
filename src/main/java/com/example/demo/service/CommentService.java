package com.example.demo.service;

import com.example.demo.constant.CommunityConstant;
import com.example.demo.dao.CommentMapper;
import com.example.demo.dao.DiscussPostMapper;
import com.example.demo.entity.Comment;
import com.example.demo.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> selectComments(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectComments(entityType,entityId,offset,limit);
    }

    public int commentsCount(int entityType,int entityId) {
        return commentMapper.commentsCount(entityType,entityId);
    }

    // 设置事务的隔离级别和传播行为
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment) {
        if(comment==null) {
            throw new IllegalArgumentException("新增评论参数错误！");
        }
        // 过滤评论内容(包括过滤html网页标记符和敏感字符)
        String html = HtmlUtils.htmlEscape(comment.getContent());
        String filter = sensitiveFilter.filter(html);
        comment.setContent(filter);
        // 新增评论
        int rows=commentMapper.insertComment(comment);


        if(comment.getEntityType()== CommunityConstant.ENTITY_TYPE_POST) {
            int updateCommentCount=commentMapper.commentsCount(CommunityConstant.ENTITY_TYPE_POST,comment.getEntityId());
            discussPostMapper.updateDiscussPost(comment.getEntityId(), updateCommentCount);
        }

        return rows;
    }

    public Comment selectCommetById(int id) {
        return commentMapper.selectCommetById(id);
    }
}
