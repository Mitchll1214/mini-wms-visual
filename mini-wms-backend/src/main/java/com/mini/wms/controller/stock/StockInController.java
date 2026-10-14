package com.mini.wms.controller.stock;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.StockInOrder;
import com.mini.wms.service.StockInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 入库控制器
 */
@RestController
@RequestMapping("/api/stock-in")
@RequiredArgsConstructor
public class StockInController {

    private final StockInService stockInService;

    /**
     * 入库单列表
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status) {
        return stockInService.page(page, size, keyword, status);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return stockInService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody StockInOrder order) {
        return stockInService.create(order);
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Integer id) {
        return stockInService.cancel(id);
    }

    @PostMapping("/{id}/audit")
    public Result<?> audit(@PathVariable Integer id,
                           @RequestParam(required = false) String operator) {
        return stockInService.audit(id, operator == null ? "admin" : operator);
    }
}