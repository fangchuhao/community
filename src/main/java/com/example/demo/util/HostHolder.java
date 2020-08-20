package com.example.demo.util;

import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用于代替session对象保存用户信息
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void remove() {
        users.remove();
    }
}
