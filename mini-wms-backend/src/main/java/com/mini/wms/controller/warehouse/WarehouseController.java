package com.mini.wms.controller.warehouse;

import com.mini.wms.dto.Result;
import com.mini.wms.service.WarehouseService;
import com.mini.wms.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/list")
    public Result<?> list() {
        return warehouseService.list();
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return warehouseService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Warehouse warehouse) {
        return warehouseService.create(warehouse);
    }

    @PutMapping
    public Result<?> update(@RequestBody Warehouse warehouse) {
        return warehouseService.update(warehouse.getId(), warehouse);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return warehouseService.delete(id);
    }
}