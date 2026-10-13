package com.javaee.documentsystem.service;

import com.javaee.documentsystem.common.PasswordUtil;
import com.javaee.documentsystem.entity.User;
import com.javaee.documentsystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    // 登录（使用 BCrypt 校验）
    public User login(String username, String rawPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        boolean matched = PasswordUtil.matches(rawPassword, user.getPassword());
        return matched ? user : null;
    }

    // 注册（密码加密存储）
    public boolean register(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }
        String encodedPassword = PasswordUtil.encode(user.getPassword());
        user.setPassword(encodedPassword);
        int rows = userMapper.insert(user);
        return rows > 0;
    }
}