package com.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * 获取当前登录用户的 userId
     */
    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getUserId();
        }
        return null;
    }

    /**
     * 获取当前登录用户的 username
     */
    public static String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getUsername();
        }
        return null;
    }

    /**
     * 获取当前登录用户的 role
     */
    public static String getCurrentRole() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getRole();
        }
        return null;
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否是 ADMIN
     */
    public static boolean isCurrentUser(Long userId) {
        Long current = getCurrentUserId();
        return current != null && current.equals(userId);
    }
}
