package com.mini.wms.controller.goods;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Goods;
import com.mini.wms.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String categoryId,
                          @RequestParam(required = false) String status) {
        return goodsService.page(page, size, keyword, categoryId, status);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return goodsService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Goods goods) {
        return goodsService.create(goods);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Goods goods) {
        return goodsService.update(id, goods);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return goodsService.delete(id);
    }

    @PostMapping("/{id}/status")
    public Result<?> status(@PathVariable Integer id, @RequestParam String status) {
        return goodsService.status(id, status);
    }
}