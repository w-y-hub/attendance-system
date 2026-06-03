package com.example.attendance.repository;

/**
 * 用户数据访问层 —— 负责 User 表的数据库操作
 *
 * 【关于 JpaRepository】
 * 这是一个"接口"，不是类。你不需要写实现代码！
 * Spring Data JPA 会在运行时自动生成实现（基于"动态代理"技术）。
 * 你只需要继承 JpaRepository<实体类型, 主键类型>，就自动获得了：
 *   findAll()、findById()、save()、deleteById() 等 20+ 个方法。
 *
 * 【方法命名规则（Query Method）】
 * Spring Data JPA 能根据方法名"猜"出你要干什么：
 *   findByUsername()       → WHERE username = ?
 *   existsByUsername()     → SELECT COUNT(*) WHERE username = ?，返回 boolean
 *   方法名 = find/exists/count + By + 字段名 + And/Or + 字段名...
 *
 * 【返回值 Optional】
 * Optional 是 Java 8 引入的，表示"可能有值，也可能没有"：
 *   Optional.of(user)     → 查到了，里面有 User 对象
 *   Optional.empty()      → 没查到
 * 用 .orElse(null) 或 .orElseThrow() 来取值。
 */

import com.example.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查找用户（用于登录验证） */
    Optional<User> findByUsername(String username);

    /** 判断用户名是否已存在（用于注册时查重） */
    boolean existsByUsername(String username);
}