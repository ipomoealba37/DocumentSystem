package com.javaee.documentsystem;

import com.javaee.documentsystem.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@MapperScan("com.javaee.documentsystem.mapper")
public class DocumentSystemApplication {

    public static void main(String[] args) {
        // 先读项目根目录的 .env（如果存在），再启动 Spring，配置里的 ${XXX} 才取得到值
        List<String> loaded = DotenvLoader.load();
        if (!loaded.isEmpty()) {
            System.out.println(".env 已载入 " + loaded.size() + " 项：" + String.join(", ", loaded));
        }
        SpringApplication.run(DocumentSystemApplication.class, args);
        System.out.println("项目启动成功！");
    }

}
