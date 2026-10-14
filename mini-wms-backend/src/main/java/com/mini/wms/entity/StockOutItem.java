package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 出库单明细（含拆分）
 */
@Data
@TableName("stock_out_item")
public class StockOutItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer goodsId;

    private Integer binId;

    private Integer qty;

    private Integer isSuggested;

    private Integer suggestOrder;
}