package com.mini.wms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.wms.entity.SysSession;
import com.mini.wms.entity.SysUser;
import com.mini.wms.mapper.SysUserMapper;
import com.mini.wms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证与权限拦截器
 * 权限矩阵：
 *   ADMIN     —— 全部权限（含用户管理、删除数据、系统配置）
 *   OPERATOR  —— 除用户管理与删除数据之外的全部权限
 *   INSPECTOR —— 仅查看（所有写操作拦截）
 * 免登录接口：/api/auth/login
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 免登录接口 */
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login"
    };

    private final AuthService authService;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 非 API 请求放行（静态资源 / SPA 页面）
        if (!uri.startsWith("/api/")) {
            return true;
        }

        // 放行免登录接口
        for (String p : PUBLIC_PATHS) {
            if (uri.equals(p)) {
                return true;
            }
        }

        // 放行图片读取：<img>/<el-image> 直接请求无法携带 X-Session-Id 头，
        // 图片文件名为随机 UUID、仅可在局域网内查看，故允许匿名读取
        if ("GET".equalsIgnoreCase(request.getMethod())
                && uri.matches("^/api/(supplier-)?images/\\d+/(thumb|content)$")) {
            return true;
        }

        // 登录校验：X-Session-Id（兼容 query 参数）
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getParameter("sessionId");
        }
        SysSession session = authService.getValidSession(sessionId);
        if (session == null) {
            return reject(response, 401, "未登录或会话已过期");
        }

        SysUser user = sysUserMapper.selectById(session.getUserId());
        if (user == null) {
            return reject(response, 401, "用户不存在");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            return reject(response, 403, "该账号已被禁用，请联系管理员");
        }

        String role = user.getRole() == null ? "OPERATOR" : user.getRole();

        // 管理员：全部权限
        if ("ADMIN".equals(role)) {
            return true;
        }

        String method = request.getMethod().toUpperCase();

        // 用户管理：仅管理员
        if (uri.startsWith("/api/auth/users")) {
            return reject(response, 403, "仅管理员可操作用户管理");
        }

        // 删除数据：仅管理员
        if ("DELETE".equals(method)) {
            return reject(response, 403, "删除数据仅管理员可执行");
        }

        // 盘点员：仅查看
        if ("INSPECTOR".equals(role) && !"GET".equals(method)) {
            return reject(response, 403, "盘点员仅可查看数据，无法执行该操作");
        }

        return true;
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code == 401 ? 401 : 403);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}