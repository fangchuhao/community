package com.example.demo.dao;

import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    // 查询当前用户会话列表，每个会话只显示最新一条消息
    List<Message> selectConversationsByFromId(int userId,int offset,int limit);

    // 查询当前用户会话数量
    int countConversationsByFromId(int userId);

    // 查询当前用户与某个用户的会话详情（即会话记录）
    List<Message> selectByConversationId(String conversationId,int offset,int limit);

    // 查询当前用户与某个用户会话的消息总数
    int countByConversationId(String conversationId);

    // 查询当前用户未读消息数量 或 与某个用户的未读消息数量
    int countUnReadMessage(int userId,String conversationId);

    // 改变消息状态
    int updateMessageStatus(int id,int status);

    // 批量改变消息状态
    int updateMessageStatusByIds(int[] ids,int status);

    // 新增信息
    int insertMessage(Message message);

    // 某一类系统消息的最新消息
    Message selectLastestByKafkaType(int userId,String topic);

    // 某一类系统消息的通知数量（包括已读未读）
    int countByKafkaType(int userId,String topic);

    // 某一类系统消息未读的数量
    int countUnReadedByKafkaType(int userId,String topic);

    // 某一类系统消息的详情
    List<Message> selectListByKafkaType(int userId,String topic,int offset,int limit);
}
