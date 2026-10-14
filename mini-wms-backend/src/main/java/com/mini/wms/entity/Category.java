package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类别实体（树形）
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 父级 ID，0 为根 */
    private Integer parentId;

    private String name;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    /** 子类别（非表字段） */
    @TableField(exist = false)
    private List<Category> children;
}