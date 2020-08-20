package com.example.demo.config;

import com.example.demo.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean postScoreRefreshDetail() {
        JobDetailFactoryBean factory=new JobDetailFactoryBean();
        factory.setJobClass(PostScoreRefreshJob.class);
        factory.setGroup("communityJobGroup");
        factory.setName("postScoreRefreshJob");
        factory.setDurability(true);
        factory.setRequestsRecovery(true);
        return factory;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefresTrigger(JobDetail postScoreRefreshDetail) {
        SimpleTriggerFactoryBean triggerFactory=new SimpleTriggerFactoryBean();
        triggerFactory.setJobDetail(postScoreRefreshDetail);
        triggerFactory.setGroup("communityTriggerGroup");
        triggerFactory.setName("postScoreRefresTrigger");
        triggerFactory.setRepeatInterval(1000*60*5);
        triggerFactory.setJobDataMap(new JobDataMap());
        return triggerFactory;
    }
}
