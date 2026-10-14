package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商资质图片实体
 */
@Data
@TableName("supplier_image")
public class SupplierImage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer supplierId;

    /** 资质类型：qualification / license / contract 等 */
    private String imageType;

    private String relativePath;

    private String thumbPath;

    private String mimeType;

    private Long fileSize;

    private String sha256;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
}