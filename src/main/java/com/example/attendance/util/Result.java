package com.example.attendance.util;

/**
 * 统一 JSON 响应对象 —— 给 AJAX / API 接口返回统一格式的数据
 *
 * 【为什么需要这个类？】
 * 如果每个接口返回的数据格式不一样（有的返回 {code, msg}，有的直接返回数据），
 * 前端处理起来会很痛苦。Result 类让所有接口返回统一的结构：
 *
 * 成功时：{ code: 200, msg: "操作成功", data: ... }
 * 失败时：{ code: 500, msg: "错误信息", data: null }
 *
 * 【泛型 <T>】
 * Result<T> 中的 T 代表 data 字段的类型。
 * Result.success("注册成功") → T 是 String，data = "注册成功"
 * Result.success(userObj)   → T 是 User，data = userObj
 *
 * 【静态工厂方法】
 * success() 和 error() 是静态方法，直接通过类名调用，
 * 不需要 new Result()。
 */

public class Result<T> {
    private Integer code;    // 状态码：200=成功，500=失败
    private String msg;      // 提示信息
    private T data;          // 实际返回的数据（泛型，可以是任意类型）

    public Result() {}

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // --- Getter / Setter ---
    public Integer getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /** 快速创建成功响应 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 快速创建失败响应 */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}
