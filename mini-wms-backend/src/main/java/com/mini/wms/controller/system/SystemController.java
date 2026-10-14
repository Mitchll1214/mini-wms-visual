package com.mini.wms.controller.system;

import com.mini.wms.dto.Result;
import com.mini.wms.service.BackupService;
import com.mini.wms.service.SysConfigService;
import com.mini.wms.service.OperationLogService;
import com.mini.wms.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 系统控制器
 * 处理系统配置、备份、日志等系统级操作
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final BackupService backupService;
    private final SysConfigService sysConfigService;
    private final OperationLogService operationLogService;
    private final FileUtil fileUtil;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 首页看板统计
     */
    @GetMapping("/stats")
    public Result<?> stats() {
        int totalGoods = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM goods WHERE status = 'ENABLED'", Integer.class);
        int totalWarehouses = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM warehouse", Integer.class);
        Integer inv = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(qty), 0) FROM inventory WHERE deleted_at IS NULL", Integer.class);
        int pendingStock = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stock_type_dict WHERE 1 = 0", Integer.class);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("totalGoods", totalGoods);
        data.put("totalWarehouses", totalWarehouses);
        data.put("totalInventory", inv);
        data.put("pendingStock", pendingStock);
        return Result.success(data);
    }

    /**
     * 获取局域网 IP
     */
    @GetMapping("/lan-ip")
    public Result<?> getLanIp() {
        String ip = getLocalIpAddress();
        return Result.success(ip);
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<?> getConfig() {
        return Result.success(sysConfigService.list());
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody String configJson) {
        // 这里应该解析JSON并更新配置
        sysConfigService.updateConfig(configJson);
        return Result.success();
    }

    /**
     * 一键备份
     */
    @PostMapping("/backup")
    public Result<?> createBackup() {
        return backupService.createBackup();
    }

    /**
     * 下载备份文件
     */
    @GetMapping("/backup/{fileName}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) throws IOException {
        Path filePath = Paths.get(fileUtil.getBackupDir(), fileName);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public Result<?> getLogs(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String module) {
        return operationLogService.page(page, size, module);
    }

    /**
     * 获取局域网 IP 地址（优先 192.168.x.x / 10.x.x.x / 172.16-31.x.x）
     */
    private String getLocalIpAddress() {
        try {
            java.net.NetworkInterface iface = java.net.NetworkInterface.getByName("以太网");
            if (iface == null) {
                iface = java.net.NetworkInterface.getByName("WLAN");
            }
            if (iface == null) {
                iface = java.net.NetworkInterface.getByName("Ethernet");
            }
            if (iface == null) {
                iface = java.net.NetworkInterface.getByName("Wi-Fi");
            }
            if (iface == null) {
                return "127.0.0.1";
            }
            java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                java.net.InetAddress addr = addresses.nextElement();
                String ipStr = addr.getHostAddress();
                if (ipStr.startsWith("192.168.") || ipStr.startsWith("10.") || ipStr.startsWith("172.")) {
                    return ipStr;
                }
            }
            return "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}