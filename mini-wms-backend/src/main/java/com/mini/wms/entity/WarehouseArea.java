package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库区实体
 */
@Data
@TableName("warehouse_area")
public class WarehouseArea {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 仓库 ID */
    private Integer warehouseId;

    private String name;

    private String remark;

    private LocalDateTime createdAt;
}