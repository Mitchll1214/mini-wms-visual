package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Data
@TableName("sys_session")
public class SysSession {

    @TableId(type = IdType.INPUT)
    private String id;

    private Integer userId;

    /** PC / MOBILE */
    private String clientType;

    /** READONLY / OPERATE */
    private String permissionMode;

    private String deviceInfo;

    private LocalDateTime createdAt;

    private LocalDateTime expireAt;
}