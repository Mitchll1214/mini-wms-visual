package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 入库单明细
 */
@Data
@TableName("stock_in_item")
public class StockInItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer goodsId;

    private Integer binId;

    private Integer qty;

    private LocalDateTime createdAt;
}