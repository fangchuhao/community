package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private Logger logger= LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.command}")
    private String command;

    @Value("${wk.image.storagePath}")
    private String storagePath;

    @PostConstruct
    public void init() {
        File file=new File(storagePath);
        if(!file.exists()) {
            file.mkdirs();
            logger.info("wk网页转图片路径创建完毕："+storagePath);
        }

    }
}
