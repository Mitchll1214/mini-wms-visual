package com.mini.wms.controller.inventory;

import com.mini.wms.dto.Result;
import com.mini.wms.service.InventoryService;
import com.mini.wms.service.StockLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 * 提供库存总览分页、商品分储位明细、储位视图、库存流水、零库存删除。
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockLogService stockLogService;

    /**
     * 商品库存分页（商品视图，合并总库存）
     * GET /api/inventory/page?page=1&size=10&goodsId=&status=
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer goodsId,
                          @RequestParam(required = false) String status) {
        return inventoryService.page(page, size, keyword, goodsId, status);
    }

    /**
     * 商品分储位明细
     * GET /api/inventory/goods/{id}
     */
    @GetMapping("/goods/{id}")
    public Result<?> goodsDetail(@PathVariable Integer id) {
        return inventoryService.goodsDetail(id);
    }

    /**
     * 某商品有库存的储位列表（出库下拉用）
     * GET /api/inventory/bins?goodsId=
     */
    @GetMapping("/bins")
    public Result<?> bins(@RequestParam Integer goodsId) {
        return inventoryService.bins(goodsId);
    }

    /**
     * 储位视图
     * GET /api/inventory/bin/{binId}
     */
    @GetMapping("/bin/{binId}")
    public Result<?> binInventory(@PathVariable Integer binId) {
        return inventoryService.binInventory(binId);
    }

    /**
     * 库存流水
     * GET /api/inventory/logs?page=1&size=10&goodsId=
     */
    @GetMapping("/logs")
    public Result<?> logs(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) Integer goodsId) {
        return stockLogService.page(page, size, goodsId);
    }

    /**
     * 零库存删除（软删除进回收站）
     * DELETE /api/inventory/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return inventoryService.deleteZeroStock(id);
    }
}