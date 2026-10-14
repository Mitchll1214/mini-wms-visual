package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
@TableName("sys_config")
public class SysConfig {

    private String configKey;

    private String configValue;

    private LocalDateTime updatedAt;
}