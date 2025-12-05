package org.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
        printStartupSuccess();
    }

    private static void printStartupSuccess() {
        String catFace =
                " /\\___/\\\n" +
                        " (='.'=)   🐾 宠物管理系统 🐾\n" +
                        " (\")_(\")   版本 1.0.0";

        System.out.println("=================================================");
        System.out.println(catFace);
        System.out.println("🚀 服务启动成功！！！");
        System.out.println("⏰ 启动时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        System.out.println("📚 在线文档: http://localhost:12345/doc.html");
        System.out.println("=================================================");
    }
}