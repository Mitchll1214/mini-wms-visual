package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
@TableName("supplier")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String contact;

    private String phone;

    private String address;

    private String status;

    private String remark;

    private LocalDateTime createdAt;
}