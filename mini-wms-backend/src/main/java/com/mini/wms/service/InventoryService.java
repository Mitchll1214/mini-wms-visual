package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Inventory;
import com.mini.wms.entity.RecycleBin;
import com.mini.wms.mapper.InventoryMapper;
import com.mini.wms.mapper.RecycleBinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final RecycleBinMapper recycleBinMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 商品库存分页（商品合并视图）
     * 每个商品一行：goodsCode/goodsName/categoryName/totalQty/binSummary/binList
     */
    public Result<?> page(int page, int size, String keyword, Integer goodsId, String status) {
        StringBuilder where = new StringBuilder(" WHERE i.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (g.name LIKE ? OR g.code LIKE ?) ");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        if (goodsId != null) {
            where.append(" AND i.goods_id = ? ");
            args.add(goodsId);
        }

        // 总数：有库存记录的商品数（去重）
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (SELECT i.goods_id FROM inventory i "
                        + "LEFT JOIN goods g ON g.id = i.goods_id" + where + " GROUP BY i.goods_id) t",
                Long.class, args.toArray());

        int offset = (page - 1) * size;
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add(offset);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT i.goods_id AS goodsId, g.code AS goodsCode, g.name AS goodsName, "
                        + "c.name AS categoryName, COALESCE(SUM(i.qty), 0) AS totalQty "
                        + "FROM inventory i "
                        + "LEFT JOIN goods g ON g.id = i.goods_id "
                        + "LEFT JOIN category c ON c.id = g.category_id"
                        + where
                        + " GROUP BY i.goods_id ORDER BY i.goods_id LIMIT ? OFFSET ?",
                pageArgs.toArray());

        // 填充每个商品的分储位明细与分布摘要
        for (Map<String, Object> r : records) {
            Integer gid = ((Number) r.get("goodsId")).intValue();
            List<Map<String, Object>> bins = jdbcTemplate.queryForList(
                    "SELECT i.id AS id, i.bin_id AS binId, b.code AS binCode, b.name AS binName, i.qty AS qty "
                            + "FROM inventory i LEFT JOIN bin b ON b.id = i.bin_id "
                            + "WHERE i.goods_id = ? AND i.deleted_at IS NULL ORDER BY i.qty DESC",
                    gid);
            r.put("binList", bins);
            r.put("binSummary", bins.stream()
                    .map(b -> (b.get("binCode") == null ? "?" : b.get("binCode")) + "(" + b.get("qty") + ")")
                    .collect(Collectors.joining("、")));
        }

        long totalCount = total == null ? 0L : total;
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", totalCount);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", size > 0 ? (totalCount + size - 1) / size : 0);
        return Result.success(result);
    }

    /**
     * 商品分储位明细
     */
    public Result<?> goodsDetail(Integer goodsId) {
        return Result.success(inventoryMapper.selectByGoodsId(goodsId));
    }

    /**
     * 储位视图
     */
    public Result<?> binInventory(Integer binId) {
        return Result.success(inventoryMapper.selectByBinId(binId));
    }

    /**
     * 商品总库存
     */
    public Result<?> goodsTotal(Integer goodsId) {
        return Result.success(inventoryMapper.selectTotalByGoodsId(goodsId));
    }

    /**
     * 某商品有库存的储位列表（出库下拉用）
     */
    public Result<?> bins(Integer goodsId) {
        return Result.success(inventoryMapper.selectBinsByGoodsId(goodsId));
    }

    /**
     * 零库存删除（软删除进回收站，30 天可恢复）
     */
    @Transactional
    public Result<?> deleteZeroStock(Integer id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, goods_id, bin_id, qty FROM inventory WHERE id = ? AND deleted_at IS NULL", id);
        if (rows.isEmpty()) {
            return Result.error("库存记录不存在或已删除");
        }
        Map<String, Object> inv = rows.get(0);
        int goodsId = ((Number) inv.get("goods_id")).intValue();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE inventory SET deleted_at = ?, deleted_expire_at = ? WHERE id = ?",
                now, now.plusDays(30), id);
        RecycleBin rb = new RecycleBin();
        rb.setBizType("inventory");
        rb.setBizId(goodsId);
        rb.setOriginalData("{\"inventoryId\":" + id + "}");
        rb.setDeletedAt(now);
        rb.setExpireAt(now.plusDays(30));
        rb.setRestored(false);
        try {
            recycleBinMapper.insert(rb);
        } catch (Exception e) {
            // UNIQUE(biz_type, biz_id) 冲突时忽略（已有回收站记录）
        }
        return Result.success("已移入回收站");
    }
}