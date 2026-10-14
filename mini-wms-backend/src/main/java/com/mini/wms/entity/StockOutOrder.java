package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单主表
 */
@Data
@TableName("stock_out_order")
public class StockOutOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private String outType;

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
    private List<StockOutItem> items;
}