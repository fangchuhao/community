package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class ThreadPoolTest {
    private Logger logger= LoggerFactory.getLogger(ThreadPoolTest.class);

    private ExecutorService executorService= Executors.newFixedThreadPool(5);

    private ScheduledExecutorService scheduledExecutorService=Executors.newScheduledThreadPool(5);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private void sleep(long m) {
        try {
            Thread.sleep(m);
        }catch (Exception e) {

        }
    }

    @Test
    public void test1() {
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService!");
            }
        };
        for(int i=0;i<10;i++) {
            executorService.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void test2() {
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService!");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task,10000,1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    @Test
    public void test3() {
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello Spring自带的ThreadPoolTaskExecutor!");
            }
        };
        for(int i=0;i<10;i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void test4() {
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello Spring自带的ThreadPoolTaskScheduler!");
            }
        };
        Date date=new Date(System.currentTimeMillis()+10000);
        taskScheduler.scheduleAtFixedRate(task,date,1000);
        sleep(30000);
    }

    @Test
    public void test5() {
        sleep(30000);
    }

    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    @Scheduled(initialDelay = 10000,fixedRate = 1000)
    public void execute2() {
        logger.debug("execute2");
    }
}
