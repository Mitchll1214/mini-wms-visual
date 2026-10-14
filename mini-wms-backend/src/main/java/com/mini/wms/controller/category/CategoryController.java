package com.mini.wms.controller.category;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Category;
import com.mini.wms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 类别控制器
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public Result<?> tree() {
        return categoryService.tree();
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Integer id) {
        return categoryService.get(id);
    }

    @PostMapping
    public Result<?> create(@RequestBody Category category) {
        return categoryService.create(category);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Category category) {
        return categoryService.update(id, category);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return categoryService.delete(id);
    }
}