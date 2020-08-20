package com.example.demo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

public class CommonUtil {
    public static String generateString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String md5Encrypt(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }


    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        if(msg!=null) {
            jsonObject.put("msg", msg);
        }

        if(map!=null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }

        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }
}