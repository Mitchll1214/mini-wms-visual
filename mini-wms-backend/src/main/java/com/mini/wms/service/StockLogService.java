package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.StockLog;
import com.mini.wms.mapper.StockLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水服务
 */
@Service
@RequiredArgsConstructor
public class StockLogService {

    private final StockLogMapper stockLogMapper;

    /**
     * 记录库存流水
     */
    public void record(Integer goodsId, Integer binId, String changeType, Integer changeQty,
                       Integer beforeQty, Integer afterQty, String adjustReason,
                       Integer refOrderId, String operator, String deviceInfo, String remark) {
        StockLog log = new StockLog();
        log.setGoodsId(goodsId);
        log.setBinId(binId);
        log.setChangeType(changeType);
        log.setChangeQty(changeQty);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setAdjustReason(adjustReason);
        log.setRefOrderId(refOrderId);
        log.setOperator(operator);
        log.setDeviceInfo(deviceInfo);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        stockLogMapper.insert(log);
    }

    /**
     * 分页查询流水
     */
    public Result<?> page(int page, int size, Integer goodsId) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<StockLog> pageObj =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<StockLog> result = stockLogMapper.selectPage(pageObj, null);
        return Result.success(result);
    }
}