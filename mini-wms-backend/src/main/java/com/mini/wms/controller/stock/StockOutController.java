package com.mini.wms.controller.stock;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.StockOutOrder;
import com.mini.wms.service.StockOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 出库控制器
 */
@RestController
@RequestMapping("/api/stock-out")
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService stockOutService;

    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status) {
        return stockOutService.page(page, size, keyword, status);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return stockOutService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody StockOutOrder order) {
        return stockOutService.create(order);
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Integer id) {
        return stockOutService.cancel(id);
    }

    @PostMapping("/{id}/audit")
    public Result<?> audit(@PathVariable Integer id,
                           @RequestParam(required = false) String operator) {
        return stockOutService.audit(id, operator == null ? "admin" : operator);
    }

    @GetMapping("/bins")
    public Result<?> getBinsByGoodsId(@RequestParam Integer goodsId) {
        return stockOutService.getBinsByGoodsId(goodsId);
    }

    @PostMapping("/suggest")
    public Result<?> suggestSplit(@RequestBody Map<String, Object> params) {
        Object goodsIdObj = params.get("goodsId");
        Object totalQtyObj = params.get("totalQty");
        Integer goodsId = goodsIdObj == null ? null : ((Number) goodsIdObj).intValue();
        Integer totalQty = totalQtyObj == null ? null : ((Number) totalQtyObj).intValue();
        return stockOutService.suggestSplit(goodsId, totalQty);
    }
}