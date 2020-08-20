package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class QuartzTest {
    @Autowired
    private Scheduler scheduler;

    @Test
    public void test1() throws SchedulerException {
        scheduler.deleteJob(new JobKey("communityJobDetail","communityJobGroup"));
    }
}
