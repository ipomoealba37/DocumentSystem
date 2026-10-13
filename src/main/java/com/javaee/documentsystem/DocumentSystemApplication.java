package com.javaee.documentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@MapperScan("com.javaee.documentsystem.mapper")
public class DocumentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentSystemApplication.class, args);
        System.out.println("项目启动成功！");
    }

}
