package com.example.demo.service;

import com.example.demo.util.RedisUtil;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 数据统计，每日的用户活跃数、访问量等
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");

    /**
     * 记录ip的访问
     * @param
     * @return
     */
    public void recordUV(String ip) {
        String format = sdf.format(new Date());
        String uvkey= RedisUtil.getUVKey(format);
        redisTemplate.opsForHyperLogLog().add(uvkey,ip);
    }

    /**
     * 获取某段时间内的访问量
     * @param
     * @return
     */
    public long calculateUV(Date start,Date end) {
        if(start==null || end==null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        List<String> keys=new ArrayList<>();
        while(!calendar.getTime().after(end)) {
            String uvKey=RedisUtil.getUVKey(sdf.format(calendar.getTime()));
            keys.add(uvKey);
            calendar.add(Calendar.DATE,1);
        }
        String unionKey=RedisUtil.getUVKey(sdf.format(start), sdf.format(end));
        redisTemplate.opsForHyperLogLog().union(unionKey,keys.toArray());
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }

    /**
     * 将指定用户加入DAU
     */
    public void recordDAU(int userId) {
        String format = sdf.format(new Date());
        String dauKey=RedisUtil.getDAUKey(format);
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    /**
     * 获取指定时间内的DAU(活跃用户数)
     */
    public long calculateDAU(Date start,Date end) {
        if(start==null || end==null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        List<byte[]> keys=new ArrayList<>();
        while(!calendar.getTime().after(end)) {
            String dauKey=RedisUtil.getDAUKey(sdf.format(calendar.getTime()));
            keys.add(dauKey.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        String unionKey=RedisUtil.getDAUKey(sdf.format(start), sdf.format(end));
        return Long.valueOf(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,unionKey.getBytes(),keys.toArray(new byte[0][0]));
                return connection.bitCount(unionKey.getBytes());
            }
        }).toString());
    }
}
