package com.example.attendance.service.security;

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
                .password(user.getPassword()) // 确保这里拿到的是数据库里的加密密文
                .roles(user.getRole().name())
                .disabled(!user.getEnabled())
                .build();
    }

}
