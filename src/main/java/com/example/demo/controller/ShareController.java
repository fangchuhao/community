package com.example.demo.controller;

import com.example.demo.config.WkConfig;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.Event;
import com.example.demo.event.EventProducer;
import com.example.demo.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController {
    private Logger logger= LoggerFactory.getLogger(WkConfig.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${wk.image.command}")
    private String command;

    @Value("${wk.image.storagePath}")
    private String storagePath;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Value("${qiniu.bucket.share.httpUrl}")
    private String shareBucketUrl;

    @RequestMapping(value = "/share",method= RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        String fileName= CommonUtil.generateString();

        Event event=new Event()
                .setTopic(CommunityConstant.KAFKA_TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");
        eventProducer.fireEvent(event);

        Map<String,Object> map=new HashMap<>();
        map.put("shareUrl",domain+"/share/image/"+fileName+".png");
        // TODO 如果是上传到七牛云服务器使用下面代码
        // map.put("shareUrl",shareBucketUrl+fileName);


        return CommonUtil.getJSONString(0,null,map);
    }

    // TODO 如果是上传的七牛云服务器，以下代码作废
    @RequestMapping(value = "/share/image/{fileName}",method = RequestMethod.GET)
    @ResponseBody
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if(StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }
        response.setContentType("image/png");
        File file=new File(storagePath+"/" + fileName);
        try {
            OutputStream os=response.getOutputStream();
            FileInputStream fis=new FileInputStream(file);
            byte[] buffer=new byte[1024];
            int len=0;
            while((len=fis.read(buffer))!=-1) {
                os.write(buffer,0,len);
            }
            logger.info("获取长图"+fileName+"成功！");
        } catch (IOException e) {
            logger.info("获取长图失败，报错信息为" + e.getMessage());
        }

    }
}
