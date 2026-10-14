package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 储位实体
 */
@Data
@TableName("bin")
public class Bin {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 货架 ID */
    private Integer shelfId;

    /** 储位编码 */
    private String code;

    /** 储位名称 */
    private String name;

    /** 状态 FREE / OCCUPIED / DISABLED */
    private String status;

    private String remark;

    private LocalDateTime createdAt;
}