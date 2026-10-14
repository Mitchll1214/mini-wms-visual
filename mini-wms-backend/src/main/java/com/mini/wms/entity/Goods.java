package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private Integer categoryId;

    private String spec;

    private String unit;

    private String barcode;

    /** ENABLED / DISABLED */
    private String status;

    private String remark;

    /** 主图访问地址（冗余展示字段，来自 goods_image image_type='main'，非数据库列） */
    @TableField(exist = false)
    private String mainImageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}