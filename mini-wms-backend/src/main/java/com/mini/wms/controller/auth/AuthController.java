package com.mini.wms.controller.auth;

import com.mini.wms.dto.LoginRequest;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.SysUser;
import com.mini.wms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理登录、退出、修改密码、会话查询、用户管理（仅 ADMIN）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword(),
                request.getClientType(), request.getDeviceInfo());
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        return authService.logout(getSessionId(request));
    }

    /**
     * 修改密码（基于当前会话用户）
     */
    @PutMapping("/change-password")
    public Result<?> changePassword(HttpServletRequest request,
                                    @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return authService.changePassword(getSessionId(request), oldPassword, newPassword);
    }

    /**
     * 获取当前会话
     */
    @GetMapping("/session")
    public Result<?> getSession(HttpServletRequest request) {
        return authService.getSession(getSessionId(request));
    }

    // ==================== 用户管理（仅 ADMIN，由拦截器校验） ====================

    /**
     * 用户分页列表
     */
    @GetMapping("/users/page")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String keyword) {
        return authService.listUsers(page, size, keyword);
    }

    /**
     * 新建用户
     */
    @PostMapping("/users")
    public Result<?> createUser(@RequestBody SysUser user) {
        return authService.createUser(user);
    }

    /**
     * 更新用户（密码/角色/启用状态）
     */
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Integer id, @RequestBody SysUser user) {
        return authService.updateUser(id, user);
    }

    /**
     * 删除用户（不能删除自己）
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        return authService.deleteUser(id, getSessionId(request));
    }

    private String getSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getParameter("sessionId");
        }
        return sessionId;
    }
}