package com.mini.wms.aspect;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.SysSession;
import com.mini.wms.entity.SysUser;
import com.mini.wms.mapper.SysUserMapper;
import com.mini.wms.service.AuthService;
import com.mini.wms.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 操作日志切面
 *
 * 统一拦截 controller 层所有「写操作」（POST / PUT / DELETE）并在成功返回后写入操作日志，
 * 做到操作无遗漏。日志只记录业务要点，<b>不记录接口路径与请求参数</b>：
 *   module   —— 业务模块（如 商品管理 / 入库管理），由请求路径前缀映射，不暴露路径原文；
 *   action   —— 业务动作（如 新增 / 审核 / 撤销 / 登录）；
 *   detail   —— 关键信息（登录记录来源 IP；单据记录单号；其余记录对象名称或 ID）；
 *   operator —— 操作人账号（登录等无会话接口回落到参数 username）；
 *   deviceInfo —— 客户端类型（PC / MOBILE）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final AuthService authService;
    private final SysUserMapper sysUserMapper;

    /** 命中「写操作」后记录日志 */
    @AfterReturning(pointcut = "execution(* com.mini.wms.controller..*.*(..))", returning = "result")
    public void recordOperation(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = currentRequest();
            if (request == null || !isWriteMethod(request.getMethod()) || !isSuccess(result)) {
                return;
            }
            String uri = request.getRequestURI();
            operationLogService.record(
                    resolveModule(uri),
                    resolveAction(uri, request.getMethod()),
                    resolveDetail(request, uri, joinPoint),
                    resolveOperator(request, joinPoint),
                    resolveDevice(request, joinPoint));
        } catch (Exception e) {
            // 记录日志失败绝不能影响主业务
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private boolean isSuccess(Object result) {
        if (result instanceof Result<?> r) {
            return r.getCode() == 0;
        }
        return true;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    /** 路径前缀 → 业务模块名（长前缀优先，避免 /api/inventory/adjust 被 /api/inventory 抢占） */
    private String resolveModule(String uri) {
        if (uri.startsWith("/api/inventory/adjust")) return "库存调整";
        if (uri.startsWith("/api/auth/users")) return "用户管理";
        if (uri.startsWith("/api/supplier-images")) return "供应商图片";
        if (uri.startsWith("/api/stock-in")) return "入库管理";
        if (uri.startsWith("/api/stock-out")) return "出库管理";
        if (uri.startsWith("/api/inventory")) return "库存管理";
        if (uri.startsWith("/api/goods")) return "商品管理";
        if (uri.startsWith("/api/category")) return "商品类别";
        if (uri.startsWith("/api/supplier")) return "供应商管理";
        if (uri.startsWith("/api/warehouse")) return "仓库管理";
        if (uri.startsWith("/api/area")) return "库区管理";
        if (uri.startsWith("/api/shelf")) return "货架管理";
        if (uri.startsWith("/api/bin")) return "储位管理";
        if (uri.startsWith("/api/recycle")) return "回收站";
        if (uri.startsWith("/api/images")) return "商品图片";
        if (uri.startsWith("/api/system")) return "系统管理";
        if (uri.startsWith("/api/auth")) return "认证";
        return "其他";
    }

    /** HTTP 方法 + 路径末段 → 业务动作（只输出动作名，不暴露路径） */
    private String resolveAction(String uri, String method) {
        if (uri.endsWith("/login")) return "登录";
        if (uri.endsWith("/logout")) return "退出登录";
        if (uri.endsWith("/change-password")) return "修改密码";
        if (uri.endsWith("/adjust")) return "库存调整";
        if (uri.endsWith("/audit")) return "审核";
        if (uri.endsWith("/cancel")) return "撤销";
        if (uri.endsWith("/restore")) return "恢复";
        if (uri.endsWith("/cleanup")) return "清理过期";
        if (uri.endsWith("/delete-zero-stock")) return "删除零库存";
        if (uri.endsWith("/status")) return "变更状态";
        if (uri.endsWith("/backup")) return "一键备份";
        if (uri.endsWith("/config")) return "更新配置";
        if (uri.endsWith("/markers")) return "图片标记";
        if ("POST".equalsIgnoreCase(method) && uri.endsWith("/images")) return "上传图片";
        if (uri.endsWith("/suggest")) return "获取拆分方案";
        if ("POST".equalsIgnoreCase(method)) return "新增";
        if ("PUT".equalsIgnoreCase(method)) return "修改";
        if ("DELETE".equalsIgnoreCase(method)) return "删除";
        return method;
    }

    /**
     * 关键信息（不含接口路径与请求参数明细）：
     *   登录 —— 记录来源 IP；
     *   单据操作 —— 记录单号；
     *   其余 —— 记录对象名称，缺失时回落为记录 ID。
     */
    private String resolveDetail(HttpServletRequest request, String uri, JoinPoint joinPoint) {
        if (uri.endsWith("/login")) {
            return "登录 IP " + clientIp(request);
        }
        String orderNo = firstGetterValue(joinPoint, "getOrderNo");
        if (orderNo != null) {
            return "单号 " + orderNo;
        }
        String name = firstGetterValue(joinPoint, "getName");
        if (name != null) {
            return "对象 " + name;
        }
        String id = lastPathNumber(uri);
        return id == null ? null : "记录 ID " + id;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /** 取请求参数对象中某个 getter 的字符串值（如 getOrderNo / getName），无则 null */
    private String firstGetterValue(JoinPoint joinPoint, String getter) {
        for (Object arg : joinPoint.getArgs()) {
            if (!isBusinessArg(arg)) {
                continue;
            }
            if (arg instanceof Map<?, ?> map) {
                Object v = map.get(getter.substring(3, 4).toLowerCase() + getter.substring(4));
                if (v != null && !v.toString().isBlank()) {
                    return v.toString();
                }
                continue;
            }
            String value = invokeStringGetter(arg, getter);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** 取请求路径中最后一段的数字（如 /api/stock-in/12/audit → 12） */
    private String lastPathNumber(String uri) {
        String[] parts = uri.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].matches("\\d+")) {
                return parts[i];
            }
        }
        return null;
    }

    /** 当前操作人：优先会话，其次请求参数里的 username（如登录接口） */
    private String resolveOperator(HttpServletRequest request, JoinPoint joinPoint) {
        String sessionId = resolveSessionId(request);
        if (sessionId != null) {
            SysSession session = authService.getValidSession(sessionId);
            if (session != null) {
                SysUser user = sysUserMapper.selectById(session.getUserId());
                if (user != null && user.getUsername() != null) {
                    return user.getUsername();
                }
            }
        }
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Map<?, ?> map) {
                Object u = map.get("username");
                if (u != null && !u.toString().isBlank()) {
                    return u.toString();
                }
            }
            String name = invokeStringGetter(arg, "getUsername");
            if (name != null) {
                return name;
            }
        }
        return "匿名";
    }

    /** 客户端类型：优先会话，其次登录参数 clientType，默认 PC */
    private String resolveDevice(HttpServletRequest request, JoinPoint joinPoint) {
        String sessionId = resolveSessionId(request);
        if (sessionId != null) {
            SysSession session = authService.getValidSession(sessionId);
            if (session != null && session.getClientType() != null && !session.getClientType().isEmpty()) {
                return session.getClientType();
            }
        }
        for (Object arg : joinPoint.getArgs()) {
            String clientType = invokeStringGetter(arg, "getClientType");
            if (clientType != null) {
                return clientType;
            }
        }
        return "PC";
    }

    private boolean isBusinessArg(Object arg) {
        if (arg == null) return false;
        if (arg instanceof String) return false;
        if (arg instanceof Number || arg instanceof Boolean) return false;
        if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) return false;
        if (arg instanceof MultipartFile || arg instanceof MultipartFile[]) return false;
        if (arg instanceof InputStream || arg instanceof OutputStream) return false;
        return true;
    }

    private String resolveSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getParameter("sessionId");
        }
        return (sessionId == null || sessionId.isEmpty()) ? null : sessionId;
    }

    private String invokeStringGetter(Object arg, String getter) {
        if (arg == null || arg instanceof String) return null;
        try {
            Method m = arg.getClass().getMethod(getter);
            Object v = m.invoke(arg);
            if (v != null && !v.toString().isBlank()) {
                return v.toString();
            }
        } catch (Exception ignore) {
            // 无该 getter
        }
        return null;
    }
}
