package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货架实体
 */
@Data
@TableName("shelf")
public class Shelf {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 库区 ID */
    private Integer areaId;

    private String name;

    private String remark;

    private LocalDateTime createdAt;
}