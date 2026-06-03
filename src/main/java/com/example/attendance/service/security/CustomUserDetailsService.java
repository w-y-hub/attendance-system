package com.example.attendance.service.security;

/**
 * Spring Security 用户认证服务
 *
 * 【UserDetailsService 接口】
 * 这是 Spring Security 定义的一个接口，只有一个方法：
 *   loadUserByUsername(String username) → UserDetails
 * Spring Security 在登录时自动调用这个方法，
 * 从数据库（或任何地方）加载用户信息。
 *
 * 【执行流程】
 * 1. 用户在登录页面输入用户名和密码，点击登录
 * 2. Spring Security 的过滤器拦截到 POST /doLogin
 * 3. 自动调用 loadUserByUsername(用户输入的用户名)
 * 4. 这个方法返回 UserDetails（包含数据库中的用户名、加密密码、角色）
 * 5. Spring Security 自动比对密码（输入的明文 vs 数据库的BCrypt密文）
 * 6. 匹配 → 登录成功，跳转到 /home
 * 7. 不匹配 → 登录失败，跳转到 /login?error
 *
 * 【关键点】
 * - 这里返回的 password 必须是数据库中已加密的密文
 * - Spring Security 会自动用 BCrypt 比对明文和密文
 * - roles() 方法会自动在角色名前加 "ROLE_" 前缀
 */

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库获取用户
        User user = userService.findByUsername(username);

        // 2. 关键修复：一定要判断 user 是否为空
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }


        // 3. 构建 UserDetails
        // 注意：.roles() 方法会自动在角色名加上 "ROLE_" 前缀
        // 如果数据库存储的是 ADMIN，这里会生成 ROLE_ADMIN
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getEnabled())
                .build();
    }

}
