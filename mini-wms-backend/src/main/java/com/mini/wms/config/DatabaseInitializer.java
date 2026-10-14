package com.mini.wms.config;

import com.mini.wms.entity.SysUser;
import com.mini.wms.mapper.SysUserMapper;
import com.mini.wms.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 数据库初始化器
 * 首次启动时执行 init.sql 建表；兼容旧库：为 sys_user 补充 role/enabled 列；
 * 预置默认管理员 admin/admin123。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private static final String DEFAULT_ADMIN_HASH =
            "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"; // admin123

    private final JdbcTemplate jdbcTemplate;
    private final SysUserMapper sysUserMapper;

    @PostConstruct
    public void init() {
        try {
            initSchema();
            migrateLegacyDb();
            ensureDefaultAdmin();
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    /** 执行 init.sql 建表（幂等：全部 CREATE TABLE IF NOT EXISTS / INSERT OR IGNORE） */
    private void initSchema() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/init.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // 按分号逐条执行，跳过 PRAGMA（已连接事务外）与空语句，便于 SQLite 兼容
        for (String stmt : sql.split(";")) {
            String trimmed = stmt.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.toUpperCase().startsWith("PRAGMA")) {
                continue;
            }
            try {
                jdbcTemplate.execute(trimmed);
            } catch (Exception e) {
                // CREATE IF NOT EXISTS 之外的语句已存在时忽略（如 INSERT 唯一冲突按 ON CONFLICT 处理）
                log.warn("执行初始化语句忽略: {}", e.getMessage());
            }
        }
    }

    /** 兼容旧库：补充新列 */
    private void migrateLegacyDb() {
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'OPERATOR'");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN enabled TINYINT NOT NULL DEFAULT 1");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE recycle_bin ADD COLUMN restored_at DATETIME");
        } catch (Exception e) {
            // 列已存在
        }
        // 单据状态列（旧库无）
        try {
            jdbcTemplate.execute("ALTER TABLE stock_in_order ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'DRAFT'");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_out_order ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'DRAFT'");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_in_item ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_in_order ADD COLUMN audit_at DATETIME");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_in_order ADD COLUMN audit_by VARCHAR(64)");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_out_order ADD COLUMN audit_at DATETIME");
        } catch (Exception e) {
            // 列已存在
        }
        try {
            jdbcTemplate.execute("ALTER TABLE stock_out_order ADD COLUMN audit_by VARCHAR(64)");
        } catch (Exception e) {
            // 列已存在
        }
        // 旧库已存在的 admin 升级为 ADMIN 角色
        jdbcTemplate.update("UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin' AND (role IS NULL OR role = '')");
        // 其余旧用户默认 OPERATOR
        jdbcTemplate.update("UPDATE sys_user SET role = 'OPERATOR' WHERE role IS NULL OR role = ''");
        // 旧用户默认启用
        jdbcTemplate.update("UPDATE sys_user SET enabled = 1 WHERE enabled IS NULL");
    }

    /** 预置默认管理员 */
    private void ensureDefaultAdmin() {
        SysUser admin = sysUserMapper.selectByUsername("admin");
        if (admin == null) {
            SysUser user = new SysUser();
            user.setUsername("admin");
            user.setPasswordHash(DEFAULT_ADMIN_HASH);
            user.setRole("ADMIN");
            user.setEnabled(1);
            sysUserMapper.insert(user);
            log.info("已创建默认管理员账号 admin");
        }
    }
}