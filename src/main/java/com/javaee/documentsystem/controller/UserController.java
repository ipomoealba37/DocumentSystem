package com.javaee.documentsystem.controller;
import com.javaee.documentsystem.common.Result;
import com.javaee.documentsystem.entity.User;
import com.javaee.documentsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //登录接口：POST /api/user/login
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user, HttpSession session){
        System.out.println("=== 收到登录请求，用户名：" + user.getUsername());
        //1.调用Service的login方法
        User loginUser = userService.login(user.getUsername(),user.getPassword());


        if (loginUser != null){
            //登录成功：把信息寸到session中
            session.setAttribute("userId",loginUser.getId());
            session.setAttribute("username",loginUser.getUsername());
           //成功：返回200+数据
            return Result.success(loginUser);
        }else {
            return Result.error(401,"用户或密码名错误");
        }

    }

    //注册接口
    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody User user){
        boolean success =userService.register(user);
        Map<String,Object> result =new HashMap<>();
        if (success){
            result.put("code",200);
            result.put("message","注册成功");
        }else {
            result.put("code",500);
            result.put("message","用户名已存在");
        }
        return result;
    }

    //获取当前登录用户
    @GetMapping("/current")
    public Map<String,Object> getCurrentUser(HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String username = (String) session.getAttribute("username");
        if (username != null){
            result.put("code",200);
            result.put("username",username);
        }else {
            result.put("code",401);
            result.put("message","未登录");
        }
        return result;
    }

    //注销
    @PostMapping("/logout")
    public Map<String,Object> logout(HttpSession session){
        session.invalidate();//清空session
        Map<String,Object> result =new HashMap<>();
        result.put("code",200);
        result.put("message","已注销");
        return result;
    }
}
