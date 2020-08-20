package com.example.demo;

import com.example.demo.service.DataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DemoApplication.class})
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1() {
        redisTemplate.opsForValue().set("miniso:count",1);

        System.out.println(redisTemplate.opsForValue().get("miniso:count"));
        System.out.println(redisTemplate.opsForValue().decrement("miniso:count"));
        System.out.println(redisTemplate.opsForValue().increment("miniso:count"));
    }

    @Test
    public void test2() {
        redisTemplate.opsForHash().put("miniso:user","username","fangchuhao");
        redisTemplate.opsForHash().put("miniso:user","id",1);


        System.out.println(redisTemplate.opsForHash().get("miniso:user","username"));
        System.out.println(redisTemplate.opsForHash().get("miniso:user","id"));
    }

    @Test
    public void test3() {
        redisTemplate.opsForList().leftPush("miniso:ids",101);
        redisTemplate.opsForList().leftPush("miniso:ids",104);
        redisTemplate.opsForList().leftPush("miniso:ids",107);
        redisTemplate.opsForList().leftPush("miniso:ids",103);
        redisTemplate.opsForList().leftPush("miniso:ids",99);

        System.out.println(redisTemplate.opsForList().index("miniso:ids",2));
        System.out.println(redisTemplate.opsForList().size("miniso:ids"));
        System.out.println(redisTemplate.opsForList().range("miniso:ids",0,4));

        System.out.println(redisTemplate.opsForList().rightPop("miniso:ids"));
        System.out.println(redisTemplate.opsForList().rightPop("miniso:ids"));
        System.out.println(redisTemplate.opsForList().rightPop("miniso:ids"));
//        System.out.println(redisTemplate.opsForList().rightPop("miniso:ids"));
//        System.out.println(redisTemplate.opsForList().rightPop("miniso:ids"));
    }

    @Test
    public void test4() {
        redisTemplate.opsForSet().add("miniso:teacher","aaa","bbb","ccc","dd","e","f","g");

        System.out.println(redisTemplate.opsForSet().size("miniso:teacher"));
        System.out.println(redisTemplate.opsForSet().members("miniso:teacher"));

        System.out.println(redisTemplate.opsForSet().pop("miniso:teacher"));
        System.out.println(redisTemplate.opsForSet().pop("miniso:teacher"));
        System.out.println(redisTemplate.opsForSet().pop("miniso:teacher"));
        System.out.println(redisTemplate.opsForSet().pop("miniso:teacher"));
        System.out.println(redisTemplate.opsForSet().pop("miniso:teacher"));

        System.out.println(redisTemplate.opsForSet().members("miniso:teacher"));
    }

    @Test
    public void test5() {
        redisTemplate.delete("miniso:teacher");
        System.out.println(redisTemplate.hasKey("miniso:teacher"));

        redisTemplate.expire("miniso:ids",5, TimeUnit.SECONDS);
        try {
            Thread.sleep(6000);
            System.out.println(redisTemplate.hasKey("miniso:ids"));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 多次访问同一个key，可以绑定这个key
     */
    @Test
    public void test6() {
        String redisKey="miniso:count";
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(redisKey);
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment(3);
        boundValueOperations.increment();
        System.out.println(boundValueOperations.get());
    }

    /**
     * 编程式事务
     */
    @Test
    public void test7() {
        Object execute = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey="miniso:teacher";
                redisOperations.multi();
                BoundSetOperations boundSetOperations = redisOperations.boundSetOps(redisKey);
                boundSetOperations.add("张三2","李四2","法考");
                System.out.println(boundSetOperations.members());
                return redisOperations.exec();
            }
        });

        System.out.println(execute);
    }

    @Test
    public void test8() {
        String key="test:h11:01";
        for(int i=1;i<=100000;i++) {
            redisTemplate.opsForHyperLogLog().add(key,i);
        }
        for(int i=1;i<=100000;i++) {
            redisTemplate.opsForHyperLogLog().add(key,(int)Math.random()*100000+1);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(key));
    }

    @Test
    public void test9() {
        String key1="test:h11:01";
        for(int i=1;i<=10000;i++) {
            redisTemplate.opsForHyperLogLog().add(key1,i);
        }
        String key2="test:h11:02";
        for(int i=5000;i<=15000;i++) {
            redisTemplate.opsForHyperLogLog().add(key2,i);
        }
        String key3="test:h11:03";
        for(int i=10000;i<=20000;i++) {
            redisTemplate.opsForHyperLogLog().add(key3,i);
        }
        String key4="test:h11:union";
        redisTemplate.opsForHyperLogLog().union(key4,key1,key2,key3);
        System.out.println(redisTemplate.opsForHyperLogLog().size(key4));
    }

    @Test
    public void test10() {
        String key1="test:h11:01";
        String key2="test:h11:02";
        String key3="test:h11:03";
        System.out.println(redisTemplate.delete(key1));
        System.out.println(redisTemplate.delete(key2));
        System.out.println(redisTemplate.delete(key3));
    }

    @Test
    public void test11() {
        String key1="test:bitmap:01";
        redisTemplate.opsForValue().setBit(key1,1,true);
        redisTemplate.opsForValue().setBit(key1,4,true);
        redisTemplate.opsForValue().setBit(key1,7,true);
        System.out.println(redisTemplate.opsForValue().getBit(key1,0));
        System.out.println(redisTemplate.opsForValue().getBit(key1,1));
        System.out.println(redisTemplate.opsForValue().getBit(key1,2));
        System.out.println(redisTemplate.opsForValue().getBit(key1,3));
        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(key1.getBytes());
            }
        }));
    }

    @Test
    public void test12() {
        // 01001001
        // 10010001
        // 00100101
        String key2="test:bitmap:02";
        redisTemplate.opsForValue().setBit(key2,1,true);
        redisTemplate.opsForValue().setBit(key2,4,true);
        redisTemplate.opsForValue().setBit(key2,7,true);
        String key3="test:bitmap:03";
        redisTemplate.opsForValue().setBit(key3,0,true);
        redisTemplate.opsForValue().setBit(key3,3,true);
        redisTemplate.opsForValue().setBit(key3,7,true);
        String key4="test:bitmap:04";
        redisTemplate.opsForValue().setBit(key4,2,true);
        redisTemplate.opsForValue().setBit(key4,5,true);
        redisTemplate.opsForValue().setBit(key4,7,true);

        String key5="test:bitmap:05";

        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitOp(RedisStringCommands.BitOperation.OR,key5.getBytes(),key2.getBytes(),key3.getBytes(),key4.getBytes());
            }
        }));

        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(key5.getBytes());
            }
        }));
    }

    @Test
    public void test13() {
        String key1="test:bitmap:01";
        String key2="test:bitmap:02";
        String key3="test:bitmap:03";
        String key4="test:bitmap:04";
        String key5="test:bitmap:05";
        System.out.println(redisTemplate.delete(key1));
        System.out.println(redisTemplate.delete(key2));
        System.out.println(redisTemplate.delete(key3));
        System.out.println(redisTemplate.delete(key4));
        System.out.println(redisTemplate.delete(key5));
    }
    @Autowired
    private DataService dataService;
    @Test
    public void test14() throws Exception {
//        dataService.recordUV("127.0.0.1");
//        dataService.recordUV("127.0.0.2");
//        dataService.recordUV("127.0.0.3");
//        dataService.recordUV("127.0.0.1");
//        SimpleDateFormat stf=new SimpleDateFormat("yyyyMMdd");
//        Date start=stf.parse("20200811");
//        Date end=stf.parse("20200813");
//        System.out.println(dataService.calculateUV(start,end));
        System.out.println(redisTemplate.delete("uv:20200813"));
    }
    @Test
    public void test15() throws ParseException {
        dataService.recordDAU(111);
        dataService.recordDAU(112);
        dataService.recordDAU(113);
        dataService.recordDAU(114);

        SimpleDateFormat stf=new SimpleDateFormat("yyyyMMdd");
        Date start=stf.parse("20200811");
        Date end=stf.parse("20200813");

        System.out.println(dataService.calculateDAU(start,end));
    }
}
