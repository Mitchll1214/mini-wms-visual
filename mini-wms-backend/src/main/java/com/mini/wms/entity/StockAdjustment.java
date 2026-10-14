package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存调整实体（用于记录调整详情）
 */
@Data
@TableName("stock_log")
public class StockAdjustment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer binId;

    private String changeType; // adjust

    private Integer changeQty; // 调整数量（可正可负）

    private Integer beforeQty;

    private Integer afterQty;

    private String adjustReason; // 调整原因：盘点差异/录入错误/损耗/丢失/赠品补录/其他

    private Integer refOrderId; // 关联单据 ID（调整无单据，可为空）

    private String operator;

    private String deviceInfo; // PC / MOBILE

    private String remark;

    private LocalDateTime createdAt;
}