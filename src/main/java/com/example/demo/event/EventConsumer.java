package com.example.demo.event;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.Event;
import com.example.demo.entity.Message;
import com.example.demo.service.DiscussPostService;
import com.example.demo.service.ElasticsearchService;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventConsumer {
    private Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String command;

    @Value("${wk.image.storagePath}")
    private String storagePath;

    // TODO 这里要注入七牛云的 accessKey secretKey bucket.share.httpUrl bucket.share.name
    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Value("${qiniu.bucket.share.httpUrl}")
    private String shareBucketUrl;

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    @KafkaListener(topics = {CommunityConstant.KAFKA_TOPIC_COMMENT,CommunityConstant.KAFKA_TOPIC_LIKE,CommunityConstant.KAFKA_TOPIC_FOLLOW})
    public void handle(ConsumerRecord consumerRecord) {
        if(consumerRecord==null || StringUtils.isBlank(consumerRecord.value().toString())) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event==null) {
            logger.error("消息格式错误！");
            return;
        }

        // entityType为帖子，用户userId评论了id为entityId的帖子，把这条消息发送给作者
        String topic = event.getTopic();
        int userId = event.getUserId();
        int entityType = event.getEntityType();
        int entityId = event.getEntityId();
        int author = event.getEntityAuthor();

        Message message=new Message();
        message.setFromId(CommunityConstant.SYSTEM_USER_ID);
        message.setToId(author);
        message.setConversationId(topic);
        message.setCreateTime(new Date());
        message.setStatus(0);

        Map<String,Object> map=new HashMap<>();
        map.put("userId",userId);
        map.put("entityType",entityType);
        map.put("entityId",entityId);

        if(!event.getData().isEmpty()) {
            for(Map.Entry<String,Object> entry: event.getData().entrySet()) {
                map.put(entry.getKey(),entry.getValue());
            }
        }

        String content=JSONObject.toJSONString(map);
        message.setContent(content);
        messageService.insertMessage(message);
    }

    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = CommunityConstant.KAFKA_TOPIC_PUBLISH)
    public void handle2(ConsumerRecord consumerRecord) {
        if(consumerRecord==null || StringUtils.isBlank(consumerRecord.value().toString())) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event==null) {
            logger.error("消息格式错误！");
            return;
        }

        int entityId = event.getEntityId();
        elasticsearchService.savePost(discussPostService.selectDisByDisId(entityId));
    }
    /**
     * 消费删帖事件
     */
    @KafkaListener(topics = CommunityConstant.KAFKA_TOPIC_DELETE)
    public void handle3(ConsumerRecord consumerRecord) {
        if(consumerRecord==null || StringUtils.isBlank(consumerRecord.value().toString())) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event==null) {
            logger.error("消息格式错误！");
            return;
        }

        int entityId = event.getEntityId();
        elasticsearchService.deletePost(entityId);
    }

    /**
     * 消费分享事件
     */
    @KafkaListener(topics = CommunityConstant.KAFKA_TOPIC_SHARE)
    public void handle4(ConsumerRecord consumerRecord) {
        if(consumerRecord==null || StringUtils.isBlank(consumerRecord.value().toString())) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event==null) {
            logger.error("消息格式错误！");
            return;
        }

        Map<String,Object> map = event.getData();
        String htmlUrl= (String) map.get("htmlUrl");
        String fileName= (String) map.get("fileName");
        String suffix= (String) map.get("suffix");
        String cmd=command + " --quality 75  " + htmlUrl + " " + storagePath+"/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("wk生成长图成功，存储路径为" + storagePath+"/" + fileName + suffix);
        }catch (Exception e) {
            logger.info("wk生成长图失败，报错信息为" + e.getMessage());
        }
        // TODO 启动定时器，监视该图片，一旦生成了，则上传至七牛云
//        UploadTask uploadTask= new UploadTask(fileName,suffix);
//        Future future = scheduler.scheduleAtFixedRate(uploadTask, 500);
//        uploadTask.setTask(future);
    }
    class UploadTask implements Runnable {
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 获取任务的返回值，可用来停止定时器
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes=1;

        public UploadTask(String fileName,String suffix) {
            this.fileName=fileName;
            this.suffix=suffix;
            startTime=System.currentTimeMillis();
        }

        public void setTask(Future future) {
            this.future=future;
        }

        @Override
        public void run() {
            // 生成图片失败
            if(System.currentTimeMillis()-startTime > 3000) {
                logger.error("执行时间过长,终止任务:"+fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if(uploadTimes > 3) {
                logger.error("上传次数过多,终止任务:"+fileName);
                future.cancel(true);
                return;
            }
            String path=storagePath+"/" + fileName + suffix;
            File file=new File(path);
            if(file.exists()) {
                logger.info(String.format("开始第%d次上传图片[%s]",uploadTimes,fileName));
                // 设置响应信息
                StringMap policy=new StringMap();
                policy.put("returnBody", CommonUtil.getJSONString(0));
                Auth auth=Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName,fileName,3600,policy);
                // 置顶上传的机房
                UploadManager uploadManager=new UploadManager(new Configuration(Zone.zone2()));
                try {
                    // 开始上传图片
                    Response response = uploadManager.put(path,fileName,uploadToken,null,"image/"+suffix,false);
                    // 处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if(jsonObject==null || jsonObject.get("code")==null || !jsonObject.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传图片[%s]失败",uploadTimes,fileName));
                    }else {
                        logger.info(String.format("第%d次上传图片[%s]成功",uploadTimes,fileName));
                        future.cancel(true);
                    }
                }catch (QiniuException e) {
                    logger.info(String.format("第%d次上传图片[%s]失败",uploadTimes,fileName));
                }
                uploadTimes++;
            }else {
                logger.info("等待长图["+fileName+"]生成中");
            }
        }
    }
}