package com.example.demo.service;

import com.example.demo.dao.MessageMapper;
import com.example.demo.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    // 查询当前用户会话列表，每个会话只显示最新一条消息
    public List<Message> selectConversationsByFromId(int userId, int offset, int limit) {
        return messageMapper.selectConversationsByFromId(userId, offset, limit);
    }

    // 查询当前用户会话数量
    public int countConversationsByFromId(int userId) {
        return messageMapper.countConversationsByFromId(userId);
    }

    // 查询当前用户与某个用户的会话详情（即会话记录）
    public List<Message> selectByConversationId(String conversationId,int offset,int limit) {
        return messageMapper.selectByConversationId(conversationId,offset,limit);
    }

    // 查询当前用户与某个用户会话的消息总数
    public int countByConversationId(String conversationId) {
        return messageMapper.countByConversationId(conversationId);
    }

    // 查询当前用户未读消息数量 或 与某个用户的未读消息数量
    public int countUnReadMessage(int userId,String conversationId) {
        return messageMapper.countUnReadMessage(userId,conversationId);
    }

    // 改变消息状态
    public int updateMessageStatus(int id,int status) {
        return messageMapper.updateMessageStatus(id,status);
    }

    // 批量改变消息状态
    public int updateMessageStatusByIds(int[] ids,int status) {
        return messageMapper.updateMessageStatusByIds(ids,status);
    }

    // 新增消息
    public int insertMessage(Message message) {
        return messageMapper.insertMessage(message);
    }

    // 某一类系统消息最新的消息
    public Message selectLastestByKafkaType(int userId,String topic) {
        return messageMapper.selectLastestByKafkaType(userId,topic);
    }

    // 某一类系统消息的通知数量（包括已读未读）
    public int countByKafkaType(int userId,String topic) {
        return messageMapper.countByKafkaType(userId,topic);
    }

    // 某一类系统消息未读的数量
    public int countUnReadedByKafkaType(int userId,String topic) {
        return messageMapper.countUnReadedByKafkaType(userId,topic);
    }

    // 某一类系统消息的详情
    public List<Message> selectListByKafkaType(int userId,String topic,int offset,int limit) {
        return messageMapper.selectListByKafkaType(userId,topic,offset,limit);
    }
}
