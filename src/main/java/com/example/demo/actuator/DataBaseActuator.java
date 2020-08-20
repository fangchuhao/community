package com.example.demo.actuator;

import com.example.demo.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Endpoint(id="database")
public class DataBaseActuator {

    private static final Logger logger= LoggerFactory.getLogger(DataBaseActuator.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String getConnection(){
        try (
                Connection connection=dataSource.getConnection();
                ){
            return CommonUtil.getJSONString(0,"获取数据库连接成功！");
        }catch (Exception e) {
            logger.error("获取数据库连接失败,"+e.getMessage());
            return CommonUtil.getJSONString(1,"获取数据库连接失败！");
        }
    }
}
