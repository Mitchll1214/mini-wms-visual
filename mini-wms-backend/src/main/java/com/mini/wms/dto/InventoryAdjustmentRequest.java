package com.mini.wms.dto;

import lombok.Data;

/**
 * 库存调整请求
 */
@Data
public class InventoryAdjustmentRequest {
    private Integer goodsId;
    private Integer binId;
    private String adjustType; // SET / INCREASE / DECREASE
    private Integer quantity; // 目标数量 或 调整数量
    private String reason; // 调整原因
    private String remark; // 备注
    private String deviceInfo; // 设备类型 PC/MOBILE
    private String operator; // 操作人
}