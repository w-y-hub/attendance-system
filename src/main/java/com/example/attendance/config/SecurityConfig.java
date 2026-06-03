package com.example.attendance.config;

/**
 * Spring Security 安全配置 —— 整个应用的"大门保安"
 *
 * 【@Configuration 注解】
 * 告诉 Spring：这个类里有一些 @Bean 方法，返回的对象要交给 Spring 容器管理。
 *
 * 【SecurityFilterChain】
 * 这是 Spring Security 的核心概念——一条"过滤器链"。
 * 每个请求进来，会依次经过这条链上的过滤器：
 *   请求 → 检查是否登录 → 检查是否有权限 → 检查 CSRF → ... → 到达 Controller
 *
 * 【初学者常见问题】
 * 1. 为什么 POST 请求返回 403？
 *    答：CSRF 保护默认开启，POST 表单必须包含 CSRF 令牌。
 *    使用 Thymeleaf 的 th:action 会自动添加，但如果用原生 <form> 就不会。
 *
 * 2. 为什么访问 /css/style.css 也被拦截？
 *    答：静态资源路径要在 .requestMatchers() 中放行，
 *    见下面 permitAll() 中的 /css/**, /js/**, /image/**, /webjars/**。
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 公开路径（无需登录）
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/image/**", "/webjars/**").permitAll()
                        // 管理员专用路径（需要 ADMIN 角色）
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 其他所有请求需要登录（无需特定角色）
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/register")
                );

        return http.build();
    }
}