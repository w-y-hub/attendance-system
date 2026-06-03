package com.example.attendance.exception;

/**
 * 全局异常处理器 —— 统一处理所有控制器抛出的异常
 *
 * 【@ControllerAdvice 注解】
 * 这是一个"通知"类，可以拦截所有 @Controller 中抛出的异常。
 * 相当于给所有控制器加了一个"catch"块。
 *
 * 【处理流程】
 * 控制器方法抛出异常
 *   ↓
 * Spring 查找 @ControllerAdvice 中能处理该异常的方法
 *   ↓
 * 找到匹配的 @ExceptionHandler 方法 → 执行
 *   ↓
 * 没找到匹配的 → 返回 HTTP 500 错误页面
 *
 * 【为什么要统一处理？】
 * 如果不统一处理，每个控制器都要自己 try-catch，
 * 代码会非常冗余，也不容易保证错误提示的一致性。
 */

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import java.security.Principal;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private UserService userService;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", "上传文件超过 10MB 限制，请压缩或拆分后重新上传");
        return redirectToReferer(request, "redirect:/home");
    }

    /**
     * 全局模型属性 —— 在所有页面的 Model 中自动添加 userRole 和 realName
     *
     * 【@ModelAttribute 注解】
     * 加了这个注解的方法，返回值会自动添加到所有控制器的 Model 中。
     * 这样每个页面都可以直接用 ${userRole} 和 ${realName}，
     * 不需要每个控制器手动 addAttribute。
     *
     * 【为什么不直接在 Controller 里加？】
     * 因为每个页面都需要知道当前用户角色来显示不同的导航栏，
     * 写在全局可以避免在每个控制器中重复同样的代码。
     */
    @ModelAttribute("userRole")
    public String getUserRole(Principal principal) {
        if (principal == null) return "STUDENT";
        try {
            com.example.attendance.entity.User user = userService.findByUsername(principal.getName());
            return user != null ? user.getRole().name() : "STUDENT";
        } catch (Exception e) {
            return "STUDENT";
        }
    }

    @ModelAttribute("realName")
    public String getRealName(Principal principal) {
        if (principal == null) return "用户";
        try {
            com.example.attendance.entity.User user = userService.findByUsername(principal.getName());
            return user != null && user.getName() != null ? user.getName() : principal.getName();
        } catch (Exception e) {
            return principal.getName();
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        log.error("系统异常", e);
        redirectAttributes.addFlashAttribute("error", "系统异常：" + e.getMessage());
        return redirectToReferer(request, "redirect:/home");
    }

    private String redirectToReferer(HttpServletRequest request, String fallback) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            // 提取路径部分，避免重定向到外部 URL
            try {
                String path = new java.net.URI(referer).getPath();
                if (path != null && !path.isEmpty()) {
                    return "redirect:" + path;
                }
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }
}