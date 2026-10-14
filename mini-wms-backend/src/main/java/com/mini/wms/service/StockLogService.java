package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.StockLog;
import com.mini.wms.mapper.StockLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存流水服务
 */
@Service
@RequiredArgsConstructor
public class StockLogService {

    private final StockLogMapper stockLogMapper;
    private final JdbcTemplate jdbcTemplate;

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
     * 分页查询流水：关联商品名称/条码与储位编码，按变动时间倒序（最新在前）
     */
    public Result<?> page(int page, int size, Integer goodsId, String changeType) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> args = new ArrayList<>();
        if (goodsId != null) {
            where.append(" AND s.goods_id = ? ");
            args.add(goodsId);
        }
        if (changeType != null && !changeType.isBlank()) {
            where.append(" AND s.change_type = ? ");
            args.add(changeType);
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_log s" + where, Long.class, args.toArray());

        int offset = (page - 1) * size;
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add(offset);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT s.id AS id, s.created_at AS createdAt, "
                        + "g.name AS goodsName, g.barcode AS barcode, b.code AS binCode, "
                        + "s.change_type AS changeType, s.change_qty AS changeQty, "
                        + "s.before_qty AS beforeQty, s.after_qty AS afterQty, "
                        + "s.operator AS operator, s.device_info AS deviceIp, "
                        + "s.adjust_reason AS adjustReason, s.remark AS remark "
                        + "FROM stock_log s "
                        + "LEFT JOIN goods g ON g.id = s.goods_id "
                        + "LEFT JOIN bin b ON b.id = s.bin_id"
                        + where
                        + " ORDER BY s.created_at DESC, s.id DESC LIMIT ? OFFSET ?",
                pageArgs.toArray());

        long totalCount = total == null ? 0L : total;
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", totalCount);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", size > 0 ? (totalCount + size - 1) / size : 0);
        return Result.success(result);
    }
}
