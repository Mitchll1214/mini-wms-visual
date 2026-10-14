package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Inventory;
import com.mini.wms.entity.StockInItem;
import com.mini.wms.entity.StockInOrder;
import com.mini.wms.mapper.InventoryMapper;
import com.mini.wms.mapper.StockInItemMapper;
import com.mini.wms.mapper.StockInOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 入库服务：新建（DRAFT）→ 审核（入库生效，动库存 + 写流水）
 */
@Service
@RequiredArgsConstructor
public class StockInService {

    private final StockInOrderMapper stockInOrderMapper;
    private final StockInItemMapper stockInItemMapper;
    private final InventoryMapper inventoryMapper;
    private final StockLogService stockLogService;

    public Result<?> page(int page, int size, String keyword, String status) {
        Page<StockInOrder> pageObj = new Page<>(page, size);
        IPage<StockInOrder> result = stockInOrderMapper.selectPage(pageObj, keyword, status);
        // 补充各单明细数量（展示用）
        for (StockInOrder order : result.getRecords()) {
            List<StockInItem> items = stockInItemMapper.selectList(
                    new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getOrderId, order.getId()));
            order.setItems(items);
        }
        return Result.success(result);
    }

    public Result<?> get(Integer id) {
        StockInOrder order = stockInOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("入库单不存在");
        }
        List<StockInItem> items = stockInItemMapper.selectList(
                new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getOrderId, id));
        order.setItems(items);
        return Result.success(order);
    }

    /**
     * 新建单据（状态 DRAFT，不立即动库存）
     */
    @Transactional
    public Result<?> create(StockInOrder order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return Result.error("请至少添加一条入库明细");
        }
        order.setOrderNo(generateOrderNo());
        order.setStatus("DRAFT");
        order.setCreatedAt(LocalDateTime.now());
        stockInOrderMapper.insert(order);
        for (StockInItem item : order.getItems()) {
            item.setId(null);
            item.setOrderId(order.getId());
            item.setCreatedAt(LocalDateTime.now());
            stockInItemMapper.insert(item);
        }
        return Result.success(order.getId());
    }

    /**
     * 审核单据：库存增加 + 写流水
     */
    @Transactional
    public Result<?> audit(Integer id, String operator) {
        StockInOrder order = stockInOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("入库单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            return Result.error("只有新建状态的单据才能审核");
        }
        List<StockInItem> items = stockInItemMapper.selectList(
                new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getOrderId, id));
        if (items.isEmpty()) {
            return Result.error("该单据没有明细，无法审核");
        }
        order.setStatus("AUDITED");
        order.setAuditAt(LocalDateTime.now().toString());
        order.setAuditBy(operator);
        stockInOrderMapper.updateById(order);

        for (StockInItem item : items) {
            int before = currentQty(item.getGoodsId(), item.getBinId());
            increaseStock(item.getGoodsId(), item.getBinId(), item.getQty());
            int after = before + item.getQty();
            stockLogService.record(item.getGoodsId(), item.getBinId(), "in", item.getQty(),
                    before, after, null, order.getId(), operator, "PC", order.getRemark());
        }
        return Result.success();
    }

    /**
     * 撤销（仅新建状态可撤销，未动库存故直接删除）
     */
    @Transactional
    public Result<?> cancel(Integer id) {
        StockInOrder order = stockInOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("入库单不存在");
        }
        if ("AUDITED".equals(order.getStatus())) {
            return Result.error("已审核的入库单不能撤销");
        }
        stockInItemMapper.delete(new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getOrderId, id));
        stockInOrderMapper.deleteById(id);
        return Result.success();
    }

    /** 库存增加（存在则累加，不存在则新建） */
    private void increaseStock(Integer goodsId, Integer binId, Integer qty) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGoodsId, goodsId)
                .eq(Inventory::getBinId, binId));
        if (inv == null || inv.getDeletedAt() != null) {
            Inventory nv = new Inventory();
            nv.setGoodsId(goodsId);
            nv.setBinId(binId);
            nv.setQty(qty);
            nv.setUpdatedAt(LocalDateTime.now());
            inventoryMapper.insert(nv);
        } else {
            inv.setQty((inv.getQty() == null ? 0 : inv.getQty()) + qty);
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryMapper.updateById(inv);
        }
    }

    private int currentQty(Integer goodsId, Integer binId) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getGoodsId, goodsId)
                .eq(Inventory::getBinId, binId));
        return inv == null || inv.getDeletedAt() != null ? 0 : (inv.getQty() == null ? 0 : inv.getQty());
    }

    private String generateOrderNo() {
        return "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}