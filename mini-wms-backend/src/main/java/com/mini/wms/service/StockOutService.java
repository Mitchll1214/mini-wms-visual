package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Inventory;
import com.mini.wms.entity.StockOutItem;
import com.mini.wms.entity.StockOutOrder;
import com.mini.wms.mapper.InventoryMapper;
import com.mini.wms.mapper.StockOutItemMapper;
import com.mini.wms.mapper.StockOutOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 出库服务：新建（DRAFT）→ 审核（出库生效，扣库存 + 写流水）；含拆分推荐
 */
@Service
@RequiredArgsConstructor
public class StockOutService {

    private final StockOutOrderMapper stockOutOrderMapper;
    private final StockOutItemMapper stockOutItemMapper;
    private final InventoryMapper inventoryMapper;
    private final StockLogService stockLogService;

    public Result<?> page(int page, int size, String keyword, String status) {
        Page<StockOutOrder> pageObj = new Page<>(page, size);
        IPage<StockOutOrder> result = stockOutOrderMapper.selectPage(pageObj, keyword, status);
        for (StockOutOrder order : result.getRecords()) {
            List<StockOutItem> items = stockOutItemMapper.selectList(
                    new LambdaQueryWrapper<StockOutItem>().eq(StockOutItem::getOrderId, order.getId()));
            order.setItems(items);
        }
        return Result.success(result);
    }

    public Result<?> get(Integer id) {
        StockOutOrder order = stockOutOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("出库单不存在");
        }
        List<StockOutItem> items = stockOutItemMapper.selectList(
                new LambdaQueryWrapper<StockOutItem>().eq(StockOutItem::getOrderId, id));
        order.setItems(items);
        return Result.success(order);
    }

    /**
     * 新建单据（状态 DRAFT，不立即动库存）
     */
    @Transactional
    public Result<?> create(StockOutOrder order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return Result.error("请至少添加一条出库明细");
        }
        order.setOrderNo(generateOrderNo());
        order.setStatus("DRAFT");
        order.setCreatedAt(LocalDateTime.now());
        stockOutOrderMapper.insert(order);
        for (StockOutItem item : order.getItems()) {
            item.setId(null);
            item.setOrderId(order.getId());
            stockOutItemMapper.insert(item);
        }
        return Result.success(order.getId());
    }

    /**
     * 审核单据：库存扣减（校验各储位库存足够）+ 写流水
     */
    @Transactional
    public Result<?> audit(Integer id, String operator) {
        StockOutOrder order = stockOutOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("出库单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            return Result.error("只有新建状态的单据才能审核");
        }
        List<StockOutItem> items = stockOutItemMapper.selectList(
                new LambdaQueryWrapper<StockOutItem>().eq(StockOutItem::getOrderId, id));
        if (items.isEmpty()) {
            return Result.error("该单据没有明细，无法审核");
        }
        // 先校验库存充足（避免扣成负数）
        for (StockOutItem item : items) {
            int current = currentQty(item.getGoodsId(), item.getBinId());
            if (current < item.getQty()) {
                return Result.error("商品库存不足：储位库存 " + current + "，需出库 " + item.getQty());
            }
        }
        order.setStatus("AUDITED");
        order.setAuditAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setAuditBy(operator);
        stockOutOrderMapper.updateById(order);

        for (StockOutItem item : items) {
            int before = currentQty(item.getGoodsId(), item.getBinId());
            decreaseStock(item.getGoodsId(), item.getBinId(), item.getQty());
            int after = before - item.getQty();
            stockLogService.record(item.getGoodsId(), item.getBinId(), "out", -item.getQty(),
                    before, after, null, order.getId(), operator, com.mini.wms.util.RequestUtil.clientIp(), order.getRemark());
        }
        return Result.success();
    }

    /**
     * 撤销（仅新建状态可撤销，未动库存故直接删除）
     */
    @Transactional
    public Result<?> cancel(Integer id) {
        StockOutOrder order = stockOutOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("出库单不存在");
        }
        if ("AUDITED".equals(order.getStatus())) {
            return Result.error("已审核的出库单不能撤销");
        }
        stockOutItemMapper.delete(new LambdaQueryWrapper<StockOutItem>().eq(StockOutItem::getOrderId, id));
        stockOutOrderMapper.deleteById(id);
        return Result.success();
    }

    /** 库存扣减 */
    private void decreaseStock(Integer goodsId, Integer binId, Integer qty) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGoodsId, goodsId)
                .eq(Inventory::getBinId, binId));
        if (inv == null || inv.getDeletedAt() != null) {
            throw new IllegalStateException("库存不存在，无法出库");
        }
        inv.setQty((inv.getQty() == null ? 0 : inv.getQty()) - qty);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.updateById(inv);
    }

    private int currentQty(Integer goodsId, Integer binId) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGoodsId, goodsId)
                .eq(Inventory::getBinId, binId));
        return inv == null || inv.getDeletedAt() != null ? 0 : (inv.getQty() == null ? 0 : inv.getQty());
    }

    /**
     * 获取商品有库存的储位列表（出库下拉用）
     */
    public Result<?> getBinsByGoodsId(Integer goodsId) {
        return Result.success(inventoryMapper.selectBinsByGoodsId(goodsId));
    }

    /**
     * 获取拆分推荐方案（按库存从多到少）
     */
    public Result<?> suggestSplit(Integer goodsId, Integer totalQty) {
        List<Map<String, Object>> result = new ArrayList<>();
        int remaining = totalQty == null ? 0 : totalQty;

        List<Map<String, Object>> bins = inventoryMapper.selectBinsByGoodsId(goodsId);
        for (int i = 0; i < bins.size() && remaining > 0; i++) {
            Map<String, Object> bin = bins.get(i);
            int binId = ((Number) bin.get("binId")).intValue();
            int currentQty = ((Number) bin.get("qty")).intValue();
            String binCode = (String) bin.get("binCode");

            int canTake = Math.min(currentQty, remaining);
            remaining -= canTake;

            Map<String, Object> item = new HashMap<>();
            item.put("binId", binId);
            item.put("binCode", binCode);
            item.put("currentQty", currentQty);
            item.put("suggestQty", canTake);
            item.put("realQty", canTake);
            item.put("suggested", true);
            item.put("suggestOrder", i + 1);
            result.add(item);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalQty", totalQty);
        summary.put("splitRemaining", remaining);
        summary.put("suggested", remaining <= 0);

        Map<String, Object> response = new HashMap<>();
        response.put("items", result);
        response.put("summary", summary);
        return Result.success(response);
    }

    private String generateOrderNo() {
        return "OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}