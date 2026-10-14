package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水实体（永久保留）
 */
@Data
@TableName("stock_log")
public class StockLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer binId;

    /** in / out / adjust */
    private String changeType;

    private Integer changeQty;

    private Integer beforeQty;

    private Integer afterQty;

    private String adjustReason;

    private Integer refOrderId;

    private String operator;

    /** PC / MOBILE */
    private String deviceInfo;

    private String remark;

    private LocalDateTime createdAt;
}