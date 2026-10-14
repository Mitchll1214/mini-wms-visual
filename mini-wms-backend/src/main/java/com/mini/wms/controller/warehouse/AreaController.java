package com.mini.wms.controller.warehouse;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.WarehouseArea;
import com.mini.wms.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库区控制器
 */
@RestController
@RequestMapping("/api/area")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping("/list")
    public Result<?> listByWarehouse(@RequestParam Integer warehouseId) {
        return areaService.listByWarehouse(warehouseId);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return areaService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody WarehouseArea area) {
        return areaService.create(area);
    }

    @PutMapping
    public Result<?> update(@RequestBody WarehouseArea area) {
        return areaService.update(area.getId(), area);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return areaService.delete(id);
    }
}