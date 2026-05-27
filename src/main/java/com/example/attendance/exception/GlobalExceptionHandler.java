package com.example.attendance.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", "上传文件超过 10MB 限制，请压缩或拆分后重新上传");
        return redirectToReferer(request, "redirect:/home");
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