package com.mini.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件路径配置
 */
@Component
@ConfigurationProperties(prefix = "mini.wms")
public class FileProperties {

    private String dataDir = "./data";
    private String imageDir = "./data/images";
    private String trashDir = "./data/trash";
    private String backupDir = "./data/backup";
    private Long maxSize = 10485760L; // 10MB
    private Integer recycleRetentionDays = 30;
    private Integer tokenTtlMinutes = 5;
    private Integer sessionTimeoutMinutes = 720;

    public String getDataDir() { return dataDir; }
    public void setDataDir(String dataDir) { this.dataDir = dataDir; }

    public String getImageDir() { return imageDir; }
    public void setImageDir(String imageDir) { this.imageDir = imageDir; }

    public String getTrashDir() { return trashDir; }
    public void setTrashDir(String trashDir) { this.trashDir = trashDir; }

    public String getBackupDir() { return backupDir; }
    public void setBackupDir(String backupDir) { this.backupDir = backupDir; }

    public Long getMaxSize() { return maxSize; }
    public void setMaxSize(Long maxSize) { this.maxSize = maxSize; }

    public Integer getRecycleRetentionDays() { return recycleRetentionDays; }
    public void setRecycleRetentionDays(Integer recycleRetentionDays) { this.recycleRetentionDays = recycleRetentionDays; }

    public Integer getTokenTtlMinutes() { return tokenTtlMinutes; }
    public void setTokenTtlMinutes(Integer tokenTtlMinutes) { this.tokenTtlMinutes = tokenTtlMinutes; }

    public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
}