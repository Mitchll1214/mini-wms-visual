package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.SysSession;
import com.mini.wms.entity.SysUser;
import com.mini.wms.mapper.SysSessionMapper;
import com.mini.wms.mapper.SysUserMapper;
import com.mini.wms.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务
 * 处理登录、退出、修改密码、扫码授权等认证业务
 * 权限体系：ADMIN（全部）/ OPERATOR（除用户管理与删除数据外的权限）/ INSPECTOR（仅查看）
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 会话超时时间（分钟） */
    private static final int SESSION_TIMEOUT_MINUTES = 720;

    private final SysUserMapper userMapper;
    private final SysSessionMapper sessionMapper;

    /**
     * 登录
     */
    public Result<?> login(String username, String password, String clientType, String deviceInfo) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.error(401, "用户名或密码不能为空");
        }
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            return Result.error(403, "该账号已被禁用，请联系管理员");
        }
        if (!PasswordUtil.hash(password).equals(user.getPasswordHash())) {
            return Result.error(401, "密码错误");
        }

        // 创建会话
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        SysSession session = new SysSession();
        session.setId(sessionId);
        session.setUserId(user.getId());
        session.setClientType(clientType == null ? "PC" : clientType);
        session.setPermissionMode("OPERATE");
        session.setDeviceInfo(deviceInfo);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpireAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
        sessionMapper.insert(session);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("permissionMode", "OPERATE");
        result.put("username", user.getUsername());
        result.put("role", user.getRole() == null ? "OPERATOR" : user.getRole());
        return Result.success(result);
    }

    /**
     * 退出登录
     */
    public Result<?> logout(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            sessionMapper.deleteById(sessionId);
        }
        return Result.success();
    }

    /**
     * 修改密码（基于当前会话的用户）
     */
    public Result<?> changePassword(String sessionId, String oldPassword, String newPassword) {
        SysUser user = getUserBySessionOrThrow(sessionId);
        if (!PasswordUtil.hash(oldPassword).equals(user.getPasswordHash())) {
            return Result.error(401, "旧密码错误");
        }
        if (newPassword == null || newPassword.length() < 4) {
            return Result.error("新密码长度不能少于4位");
        }
        user.setPasswordHash(PasswordUtil.hash(newPassword));
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 获取当前会话
     */
    public Result<?> getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Result.error(401, "未登录");
        }
        SysSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getExpireAt().isBefore(LocalDateTime.now())) {
            return Result.error(401, "会话失效");
        }
        SysUser user = userMapper.selectById(session.getUserId());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("username", user.getUsername());
        result.put("clientType", session.getClientType());
        result.put("permissionMode", session.getPermissionMode());
        result.put("role", user.getRole() == null ? "OPERATOR" : user.getRole());
        return Result.success(result);
    }

    // ==================== 用户管理（仅 ADMIN） ====================

    /**
     * 用户分页列表
     */
    public Result<?> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(SysUser::getUsername, keyword))
                .orderByDesc(SysUser::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> pageObj =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<SysUser> result = userMapper.selectPage(pageObj, wrapper);
        // 不返回密码哈希
        result.getRecords().forEach(u -> u.setPasswordHash(null));
        return Result.success(result);
    }

    /**
     * 新建用户
     */
    public Result<?> createUser(SysUser user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("OPERATOR");
        }
        if (user.getEnabled() == null) {
            user.setEnabled(1);
        }
        user.setPasswordHash(PasswordUtil.hash(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        user.setPasswordHash(null);
        return Result.success(user);
    }

    /**
     * 更新用户（用户名/密码/角色/启用状态）
     */
    public Result<?> updateUser(Integer id, SysUser user) {
        SysUser target = userMapper.selectById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            // 密码字段传明文，前端约定字段名 passwordHash 传入新密码原文
            target.setPasswordHash(PasswordUtil.hash(user.getPasswordHash()));
        }
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            target.setRole(user.getRole());
        }
        if (user.getEnabled() != null) {
            target.setEnabled(user.getEnabled());
        }
        userMapper.updateById(target);
        return Result.success();
    }

    /**
     * 删除用户（不允许删除自己；用户被会话引用时顺带清理会话）
     */
    public Result<?> deleteUser(Integer id, String operatorSessionId) {
        SysUser current = getUserBySessionOrThrow(operatorSessionId);
        if (current.getId().equals(id)) {
            return Result.error("不能删除当前登录账号");
        }
        if (userMapper.selectById(id) == null) {
            return Result.error("用户不存在");
        }
        userMapper.deleteById(id);
        // 清理该用户的会话
        sessionMapper.delete(new LambdaQueryWrapper<SysSession>().eq(SysSession::getUserId, id));
        return Result.success();
    }

    // ==================== 会话查询 ====================

    /**
     * 根据会话查询用户，无效会话抛出业务错误（返回 Result error）
     */
    private SysUser getUserBySessionOrThrow(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("未登录");
        }
        SysSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("会话失效，请重新登录");
        }
        SysUser user = userMapper.selectById(session.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    /**
     * 供拦截器使用的会话查询
     */
    public SysSession getValidSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }
        SysSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getExpireAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return session;
    }
}