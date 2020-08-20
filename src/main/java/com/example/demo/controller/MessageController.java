package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.Message;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查找当前用户的所有会话
     * @param page
     * @return
     */
    @RequestMapping("/letter/list")
    public String getConversations(Model model,Page page) {
        int userId=hostHolder.getUser().getId();

        // 设置每页数量
        page.setLimit(5);
        // 设置查看全部会话页的URL
        page.setUrl("/letter/list");
        int total = messageService.countConversationsByFromId(userId);
        // 设置分页的总数量
        page.setTotal(total);

        List<Message> messages = messageService.selectConversationsByFromId(userId, page.getOffet(), page.getLimit());
        List<Map<String,Object>> conversations=new ArrayList<>();

        // 显示全部会话的时候，每个会话需要的数据：对方的名字，会话的最新一条内容和时间，与对方总共的消息数，未读的消息数
        for(Message conversation:messages) {
            Map<String,Object> map=new HashMap<>();
            // 与对方总共的消息数
            int messageNum = messageService.countByConversationId(conversation.getConversationId());
            map.put("messageNum",messageNum);
            // 会话的最新一条内容和时间
            map.put("conversation",conversation);
            // 未读的消息数
            int unReadMessageNum=messageService.countUnReadMessage(userId,conversation.getConversationId());
            map.put("unReadMessageNum",unReadMessageNum);
            // 对方的名字
            int targetId= conversation.getFromId()==userId?conversation.getToId():conversation.getFromId();
            User targetUser = userService.selectById(targetId);
            map.put("targetUser",targetUser);
            conversations.add(map);
        }
        model.addAttribute("conversations",conversations);
        // 总共的未读消息
        int unReadMessageTotal=messageService.countUnReadMessage(userId,null);
        model.addAttribute("unReadMessageTotal",unReadMessageTotal);
        model.addAttribute("totalUnReadNotice",messageService.countUnReadedByKafkaType(userId,null));
        return "site/letter";
    }

    @RequestMapping(value="/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String letterDetail(Model model,@PathVariable("conversationId") String conversationId,Page page) {
        int userId=hostHolder.getUser().getId();
        page.setLimit(5);
        page.setUrl("/letter/detail/"+conversationId);
        int total=messageService.countByConversationId(conversationId);
        page.setTotal(total);

        List<Message> messages=messageService.selectByConversationId(conversationId,page.getOffet(),page.getLimit());
        List<Map<String,Object>> messagesMap=new ArrayList<>();

        for(Message message:messages) {
            Map<String,Object> map=new HashMap<>();
            // 修改消息状态为已读
            if(message.getToId()==userId && message.getStatus()==0) {
                messageService.updateMessageStatus(message.getId(), CommunityConstant.MESSAGE_READED);
            }

            // 获取消息实体（包括消息发送时间、消息内容等）
            map.put("message",message);
            int fromId = message.getFromId();
            // 获取消息的 发送方
            User fromUser=userService.selectById(fromId);
            map.put("fromUser",fromUser);
            messagesMap.add(map);
        }
        User targetUser = getMessageTargetUser(conversationId);
        model.addAttribute("messagesMap",messagesMap);
        model.addAttribute("targetUser",targetUser);
        return "site/letter-detail";
    }

    /**
     * 获取 当前会话的对方用户
     * @param conversationId
     * @return
     */
    private User getMessageTargetUser(String conversationId) {
        String[] split = conversationId.split("_");
        int currentLoginUserId = hostHolder.getUser().getId();
        if(String.valueOf(currentLoginUserId).equals(split[0])) {
            return userService.selectById(Integer.parseInt(split[1]));
        }
        return userService.selectById(Integer.parseInt(split[0]));
    }

    @RequestMapping(value = "/letter/add",method = RequestMethod.POST)
    @ResponseBody
    public String add(String content,String targetName) {
        User targetUser=userService.selectByName(targetName);
        if(targetUser==null) {
            return CommonUtil.getJSONString(1,"用户不存在！");
        }
        Message message=new Message();
        int toId=targetUser.getId();
        int fromId=hostHolder.getUser().getId();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setCreateTime(new Date());
        message.setStatus(0);
        content= HtmlUtils.htmlEscape(content);
        content= sensitiveFilter.filter(content);
        message.setContent(content);
        String conversationId=fromId<=toId? fromId+"_"+toId:toId+"_"+fromId;
        message.setConversationId(conversationId);
        messageService.insertMessage(message);

        return CommonUtil.getJSONString(0,"发送消息成功！");
    }

    @RequestMapping("/notice/list")
    public String notice(Model model) {
        User loginUser = hostHolder.getUser();
        if(loginUser==null) {
            throw new IllegalArgumentException("用户未登录！");
        }
        // 评论
        Message message = messageService.selectLastestByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_COMMENT);

        Map<String,Object> messageVo;
        if(message!=null) {
            messageVo=new HashMap<>();
            messageVo.put("message",message);
            int count = messageService.countByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_COMMENT);
            messageVo.put("count",count);
            int unReadCount = messageService.countUnReadedByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_COMMENT);
            messageVo.put("unReadCount",unReadCount);
            String content = message.getContent();
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            User user = userService.selectById((Integer) data.get("userId"));
            messageVo.put("user",user);
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            model.addAttribute("commentNotice",messageVo);
        }

        // 点赞
        message = messageService.selectLastestByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_LIKE);

        if(message!=null) {
            messageVo=new HashMap<>();
            messageVo.put("message",message);
            int count = messageService.countByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_LIKE);
            messageVo.put("count",count);
            int unReadCount = messageService.countUnReadedByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_LIKE);
            messageVo.put("unReadCount",unReadCount);
            String content = message.getContent();
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            User user = userService.selectById((Integer) data.get("userId"));
            messageVo.put("user",user);
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            model.addAttribute("likeNotice",messageVo);
        }


        // 关注
        message = messageService.selectLastestByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_FOLLOW);
        if(message!=null) {
            messageVo=new HashMap<>();
            messageVo.put("message",message);
            int count = messageService.countByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_FOLLOW);
            messageVo.put("count",count);
            int unReadCount = messageService.countUnReadedByKafkaType(loginUser.getId(), CommunityConstant.KAFKA_TOPIC_FOLLOW);
            messageVo.put("unReadCount",unReadCount);
            String content = message.getContent();
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            User user = userService.selectById((Integer) data.get("userId"));
            messageVo.put("user",user);
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            model.addAttribute("followNotice",messageVo);
        }


        // 查询未读消息数量
        model.addAttribute("unReadMessageTotal",messageService.countUnReadMessage(loginUser.getId(),null));
        model.addAttribute("totalUnReadNotice",messageService.countUnReadedByKafkaType(loginUser.getId(),null));
        return "site/notice";
    }

    @RequestMapping(value="/notice/detail/{topic}",method = RequestMethod.GET)
    public String noticeDetail(Model model,Page page,@PathVariable("topic") String topic) {
        User loginUser = hostHolder.getUser();
        if(loginUser==null) {
            throw new IllegalArgumentException("用户未登录！");
        }
        page.setLimit(3);
        page.setUrl("/notice/detail/"+topic);
        page.setTotal(messageService.countByKafkaType(loginUser.getId(), topic));

        List<Message> messages = messageService.selectListByKafkaType(loginUser.getId(), topic, page.getOffet(), page.getLimit());
        List<Map<String,Object>> messageVo=new ArrayList<>();
        if(messages!=null) {
            for(Message message:messages) {
                Map<String,Object> map=new HashMap<>();

                map.put("message",message);

                String content = message.getContent();
                Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
                User user = userService.selectById((Integer) data.get("userId"));

                map.put("user",user);
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                messageService.updateMessageStatus(message.getId(),1);
                messageVo.add(map);
            }
        }

        model.addAttribute("messages",messageVo);
        return "site/notice-detail";
    }
}
