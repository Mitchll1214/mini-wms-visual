package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收站实体
 */
@Data
@TableName("recycle_bin")
public class RecycleBin {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** inventory / image / marker */
    private String bizType;

    private Integer bizId;

    /** 原始数据（JSON） */
    private String originalData;

    private LocalDateTime deletedAt;

    /** 30 天后到期 */
    private LocalDateTime expireAt;

    private Boolean restored;

    private LocalDateTime restoredAt;
}