package com.mini.wms.controller.warehouse;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Bin;
import com.mini.wms.service.BinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 储位控制器
 */
@RestController
@RequestMapping("/api/bin")
@RequiredArgsConstructor
public class BinController {

    private final BinService binService;

    @GetMapping("/list")
    public Result<?> listByShelf(@RequestParam Integer shelfId) {
        return binService.listByShelf(shelfId);
    }

    @GetMapping("/tree")
    public Result<?> tree() {
        return binService.tree();
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return binService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Bin bin) {
        return binService.create(bin);
    }

    @PutMapping
    public Result<?> update(@RequestBody Bin bin) {
        return binService.update(bin.getId(), bin);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return binService.delete(id);
    }

    @GetMapping("/qrcode/{id}")
    public Result<?> qrcode(@PathVariable Integer id) {
        return binService.qrcode(id);
    }
}