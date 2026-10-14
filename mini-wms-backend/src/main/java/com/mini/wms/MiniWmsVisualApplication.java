package com.mini.wms;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * MiniWMS Visual 主程序入口
 * 小仓图管 - 面向小仓库的货位可视化库存管理系统
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.mini.wms.mapper")
public class MiniWmsVisualApplication extends SpringBootServletInitializer {

    @Value("${server.port:8080}")
    private int port;

    public static void main(String[] args) {
        // 统一使用上海时区：所有 LocalDateTime 记录均为北京时间
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
        // 确保数据目录存在（SQLite 不会自动创建父目录，首次运行需预建，否则报 Failed to obtain JDBC Connection）
        mkdirs("data", "data/images", "data/trash", "data/backup");
        SpringApplication.run(MiniWmsVisualApplication.class, args);
    }

    private static void mkdirs(String... paths) {
        for (String p : paths) {
            try {
                java.nio.file.Files.createDirectories(java.nio.file.Paths.get(p));
            } catch (Exception ignored) {
                // 创建失败时由后续数据库初始化报错提示
            }
        }
    }

    @PostConstruct
    public void init() {
        String ip = getLocalIpAddress();
        log.info("===========================================");
        log.info("  MiniWMS Visual (小仓图管) v1.0 已启动");
        log.info("  服务地址: http://localhost:{}/", port);
        log.info("  局域网地址: http://{}:{}/", ip, port);
        log.info("===========================================");
    }

    /**
     * 获取局域网 IP 地址（优先 192.168.x.x / 10.x.x.x / 172.16-31.x.x）
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String ipStr = address.getHostAddress();
                    if (!ipStr.startsWith("127.") && !ipStr.contains(":")) {
                        if (ipStr.startsWith("192.168.") || ipStr.startsWith("10.") ||
                                ipStr.startsWith("172.")) {
                            return ipStr;
                        }
                    }
                }
            }
            return "127.0.0.1";
        } catch (SocketException e) {
            log.error("获取局域网 IP 失败", e);
            return "127.0.0.1";
        }
    }
}