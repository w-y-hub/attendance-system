package com.example.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目入口 —— 程序从这开始运行
 *
 * 【@SpringBootApplication】
 * 这是一个"复合注解"，相当于三个注解加在一起：
 *   1. @SpringBootConfiguration  → 标记这是一个配置类
 *   2. @EnableAutoConfiguration  → 让 Spring Boot 自动配置（根据依赖猜你要干什么）
 *   3. @ComponentScan            → 自动扫描当前包及其子包，找到所有 @Controller、@Service 等
 *
 * 【main 方法】
 * SpringApplication.run() = 启动 Spring Boot 内嵌的 Tomcat 服务器，
 * 然后加载所有配置、创建所有 Bean，等着接收 HTTP 请求。
 *
 * 启动后访问 http://localhost:8080 即可看到页面。
 *
 * 【注意】
 * 这个类要放在最外层包（com.example.attendance）下，
 * 因为 @ComponentScan 默认从它所在的包开始扫描，
 * 如果放错位置，@Controller、@Service 等不会被识别。
 */

@SpringBootApplication
public class AttendanceSystemApplication {

    public static void main(String[] args) {
        // 这行代码 = 启动 Spring 容器 + 启动内嵌 Tomcat
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }
}
