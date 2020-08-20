package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void test1() {
        String topic="test";
        kafkaProducer.sendMessage(topic,"小猪在华农的闺房");
        kafkaProducer.sendMessage(topic,"你好456");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String msg) {
        kafkaTemplate.send(topic,msg);
    }
}
@Component
class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    public void handle(ConsumerRecord consumerRecord) {
        System.out.println("消费者获取消息："+consumerRecord.value());
    }
}