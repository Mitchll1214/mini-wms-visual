package com.mini.wms.service;

import com.mini.wms.dto.InventoryAdjustmentRequest;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Inventory;
import com.mini.wms.mapper.InventoryMapper;
import com.mini.wms.mapper.StockLogMapper;
import com.mini.wms.service.StockLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 库存调整服务
 * 仅主程序电脑可见，手机端拦截
 */
@Service
@RequiredArgsConstructor
public class StockAdjustService {

    private final InventoryMapper inventoryMapper;
    private final StockLogMapper stockLogMapper;

    /**
     * 库存调整
     */
    @Transactional
    public Result<?> adjust(InventoryAdjustmentRequest request) {
        Integer goodsId = request.getGoodsId();
        Integer binId = request.getBinId();
        Integer quantity = request.getQuantity();
        String adjustType = request.getAdjustType() == null ? "SET" : request.getAdjustType();
        String reason = request.getReason();
        String operator = request.getOperator() == null ? "admin" : request.getOperator();

        if (goodsId == null || binId == null || quantity == null) {
            return Result.error("参数不完整或无效");
        }
        if (reason == null || reason.isEmpty()) {
            return Result.error("调整原因必填");
        }

        // 1. 查询当前库存
        Inventory inventory = inventoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getGoodsId, goodsId)
                        .eq(Inventory::getBinId, binId)
                        .isNull(Inventory::getDeletedAt));

        int beforeQty = inventory != null ? (inventory.getQty() == null ? 0 : inventory.getQty()) : 0;

        // 1.1 按调整方式计算目标数量：SET 指定数量 / INCREASE 增加 / DECREASE 减少
        int targetQty;
        if ("INCREASE".equals(adjustType)) {
            targetQty = beforeQty + quantity;
        } else if ("DECREASE".equals(adjustType)) {
            targetQty = beforeQty - quantity;
        } else {
            targetQty = quantity;
        }
        if (targetQty < 0) {
            return Result.error("调整后的库存数量不能为负");
        }

        // 2. 更新库存（不存在则新建）
        int changeQty = targetQty - beforeQty;
        if (inventory == null) {
            Inventory newInv = new Inventory();
            newInv.setGoodsId(goodsId);
            newInv.setBinId(binId);
            newInv.setQty(targetQty);
            newInv.setUpdatedAt(LocalDateTime.now());
            inventoryMapper.insert(newInv);
        } else {
            inventory.setQty(targetQty);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        }

        // 3. 写入库存流水
        com.mini.wms.entity.StockLog log = new com.mini.wms.entity.StockLog();
        log.setGoodsId(goodsId);
        log.setBinId(binId);
        log.setChangeType("adjust");
        log.setChangeQty(changeQty);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(targetQty);
        log.setAdjustReason(reason);
        log.setOperator(operator);
        log.setDeviceInfo(com.mini.wms.util.RequestUtil.clientIp());
        log.setRemark(request.getRemark());
        log.setCreatedAt(LocalDateTime.now());
        stockLogMapper.insert(log);

        // 4. 返回调整结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("goodsId", goodsId);
        result.put("binId", binId);
        result.put("beforeQty", beforeQty);
        result.put("afterQty", targetQty);
        result.put("reason", reason);
        return Result.success(result);
    }

    /**
     * 验证是否为主程序电脑操作
     */
    public Result<?> validatePcOperation(String deviceInfo) {
        if (deviceInfo == null || !"PC".equals(deviceInfo)) {
            return Result.error("请在主程序电脑上操作库存调整");
        }
        return Result.success();
    }
}