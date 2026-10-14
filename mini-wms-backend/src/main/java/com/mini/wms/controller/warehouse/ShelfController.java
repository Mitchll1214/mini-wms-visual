package com.mini.wms.controller.warehouse;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Shelf;
import com.mini.wms.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 货架控制器
 */
@RestController
@RequestMapping("/api/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;

    @GetMapping("/list")
    public Result<?> listByArea(@RequestParam Integer areaId) {
        return shelfService.listByArea(areaId);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return shelfService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Shelf shelf) {
        return shelfService.create(shelf);
    }

    @PutMapping
    public Result<?> update(@RequestBody Shelf shelf) {
        return shelfService.update(shelf.getId(), shelf);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return shelfService.delete(id);
    }
}