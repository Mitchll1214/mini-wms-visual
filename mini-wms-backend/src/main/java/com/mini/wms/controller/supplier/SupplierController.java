package com.mini.wms.controller.supplier;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Supplier;
import com.mini.wms.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商控制器
 */
@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/list")
    public Result<?> list() {
        return supplierService.list();
    }

    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword) {
        return supplierService.page(page, size, keyword);
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return supplierService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Supplier supplier) {
        return supplierService.create(supplier);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Supplier supplier) {
        return supplierService.update(id, supplier);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return supplierService.delete(id);
    }
}