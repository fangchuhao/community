package com.example.demo;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sun.applet.Main;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class BlockingQueueTest {


    public static void main(String[] args) {
        BlockingQueue<Integer> blockingQueue=new ArrayBlockingQueue<Integer>(10);
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
    }
}
class Producer implements Runnable {
    private BlockingQueue<Integer> blockingQueue;

    public Producer(BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue=blockingQueue;

    }

    @Override
    public void run() {
        try {
            for(int i=0;i<200;i++) {
                Thread.sleep(20);
                blockingQueue.put(i);
                System.out.println(Thread.currentThread().getName()+"生产："+blockingQueue.size());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class Consumer implements Runnable {
    private BlockingQueue<Integer> blockingQueue;

    public Consumer(BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue=blockingQueue;

    }

    @Override
    public void run() {
        try {
            for(int i=0;i<100;i++) {
                Thread.sleep(new Random().nextInt(1000));
                blockingQueue.take();
                System.out.println(Thread.currentThread().getName()+"消费："+blockingQueue.size());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
