package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单主表
 */
@Data
@TableName("stock_in_order")
public class StockInOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private String inType;

    private Integer supplierId;

    /** DRAFT / AUDITED */
    private String status;

    private String auditAt;

    private String auditBy;

    private String remark;

    private String operator;

    private String deviceInfo;

    private LocalDateTime createdAt;

    /** 明细（非表字段，创建/详情返回用） */
    @TableField(exist = false)
    private List<StockInItem> items;
}