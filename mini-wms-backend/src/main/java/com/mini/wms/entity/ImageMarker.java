package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片标记实体
 */
@Data
@TableName("image_marker")
public class ImageMarker {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer imageId;

    private Integer binId;

    private Integer x;

    private Integer y;

    private Integer w;

    private Integer h;

    private String label;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
