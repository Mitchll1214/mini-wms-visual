package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Category;
import com.mini.wms.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 类别服务
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 类别树
     */
    public Result<?> tree() {
        List<Category> all = categoryMapper.selectAllTree();
        return Result.success(buildTree(all, 0));
    }

    public Result<?> get(Integer id) {
        return Result.success(categoryMapper.selectById(id));
    }

    public Result<?> create(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setCreatedAt(java.time.LocalDateTime.now());
        categoryMapper.insert(category);
        return Result.success(category);
    }

    public Result<?> update(Integer id, Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success(category);
    }

    public Result<?> delete(Integer id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }

    private List<Category> buildTree(List<Category> all, Integer parentId) {
        return all.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(c -> {
                    c.setChildren(buildTree(all, c.getId()));
                    return c;
                })
                .collect(Collectors.toList());
    }
}