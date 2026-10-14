package com.mini.wms.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 避免在每个业务方法里手工调用 {@link OperationLogService#record}，做到操作无遗漏。
 *
 * 日志字段：
 *   module   —— 由请求路径前缀映射（如 商品管理 / 入库管理）
 *   action   —— 由 HTTP 方法与路径末段推断（如 新增 / 修改 / 审核 / 撤销）
 *   detail   —— 请求路径 + 查询串 + 请求参数（自动脱敏密码 / 会话串）
 *   operator —— 当前会话用户（登录等无会话接口回落到参数里的 username）
 *   deviceInfo —— 会话客户端类型（PC / MOBILE）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final AuthService authService;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 命中「写操作」后记录日志 */
    @AfterReturning(pointcut = "execution(* com.mini.wms.controller..*.*(..))", returning = "result")
    public void recordOperation(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = currentRequest();
            if (request == null) {
                return;
            }
            String method = request.getMethod();
            if (!isWriteMethod(method)) {
                return;
            }
            // 仅当业务成功（Result.code == 0）时记录，失败操作由前端提示即可
            if (!isSuccess(result)) {
                return;
            }

            String uri = request.getRequestURI();
            String operator = resolveOperator(request, joinPoint);
            String device = resolveDevice(request, joinPoint);
            String detail = resolveDetail(request, joinPoint);

            operationLogService.record(resolveModule(uri), resolveAction(uri, method), detail, operator, device);
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
        // 非统一返回体（如 ResponseEntity）默认视为成功
        return true;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    /** 路径前缀 → 模块名（长前缀优先，避免 /api/inventory/adjust 被 /api/inventory 抢占） */
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

    /** HTTP 方法 + 路径末段 → 动作 */
    private String resolveAction(String uri, String method) {
        // 特殊动作（按路径末段）
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
        // 常规动作（按 HTTP 方法）
        if ("POST".equalsIgnoreCase(method)) return "新增";
        if ("PUT".equalsIgnoreCase(method)) return "修改";
        if ("DELETE".equalsIgnoreCase(method)) return "删除";
        return method;
    }

    /** 当前操作用户：优先会话，其次请求参数里的 username（如登录接口） */
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
        // 回落：从参数对象里取 username
        for (Object arg : joinPoint.getArgs()) {
            String name = extractUsername(arg);
            if (name != null) {
                return name;
            }
        }
        return "匿名";
    }

    /** 设备类型：优先会话客户端类型，其次登录参数 clientType，默认 PC */
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

    /** 详细说明：路径 + 查询串 + 可序列化参数（脱敏后截断） */
    private String resolveDetail(HttpServletRequest request, JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            sb.append('?').append(query);
        }
        for (Object arg : joinPoint.getArgs()) {
            if (!isSerializableArg(arg)) {
                continue;
            }
            try {
                String json = objectMapper.writeValueAsString(arg);
                if (json != null && json.length() > 2 && !"{}".equals(json) && !"[]".equals(json)) {
                    sb.append(" | ").append(json);
                }
            } catch (Exception ignore) {
                // 参数无法序列化则跳过
            }
        }
        String detail = desensitize(sb.toString());
        return detail.length() > 1000 ? detail.substring(0, 1000) + "…" : detail;
    }

    private boolean isSerializableArg(Object arg) {
        if (arg == null) return false;
        if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) return false;
        if (arg instanceof MultipartFile || arg instanceof MultipartFile[]) return false;
        if (arg instanceof InputStream || arg instanceof OutputStream) return false;
        return true;
    }

    /** 对密码 / 会话串等敏感字段脱敏 */
    private String desensitize(String text) {
        if (text == null) return null;
        String result = text.replaceAll(
                "(?i)\"([a-z]*password[a-z]*|oldPassword|newPassword)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"");
        result = result.replaceAll("(?i)(sessionId=)[^&\\s\"]*", "$1***");
        result = result.replaceAll("(?i)\"(sessionId)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"");
        return result;
    }

    private String resolveSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getParameter("sessionId");
        }
        return (sessionId == null || sessionId.isEmpty()) ? null : sessionId;
    }

    private String extractUsername(Object arg) {
        if (arg == null) return null;
        if (arg instanceof String) {
            return null;
        }
        if (arg instanceof Map<?, ?> map) {
            Object u = map.get("username");
            return u == null ? null : u.toString();
        }
        return invokeStringGetter(arg, "getUsername");
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
