package com.javaee.documentsystem.mapper;
import com.javaee.documentsystem.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    //注册时检查用户名是否存在
    User findByUsername(@Param("username")String username);
    //登录验证
    User login(@Param("username")String username,@Param("password")String password);
    //注册新用户
    int insert(User user);
}
