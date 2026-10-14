package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片实体
 */
@Data
@TableName("goods_image")
public class GoodsImage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    /** goods / main / bin / supplier：商品相册 / 商品主图 / 储位图 / 供应商资质 */
    private String imageType;

    private Integer binId;

    private String relativePath;

    private String thumbPath;

    private String mimeType;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private String sha256;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
}