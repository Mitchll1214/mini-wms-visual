package com.mini.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Supplier;
import com.mini.wms.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

/**
 * 供应商服务
 */
@Service
public class SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    public Result<?> list() {
        return Result.success(supplierMapper.selectList(null));
    }

    public Result<?> page(int page, int size, String keyword) {
        Page<Supplier> pageObj = new Page<>(page, size);
        IPage<Supplier> result = supplierMapper.selectPage(pageObj, null);
        return Result.success(result);
    }

    public Result<?> get(Integer id) {
        return Result.success(supplierMapper.selectById(id));
    }

    public Result<?> create(Supplier supplier) {
        supplier.setCreatedAt(java.time.LocalDateTime.now());
        supplierMapper.insert(supplier);
        return Result.success(supplier);
    }

    public Result<?> update(Integer id, Supplier supplier) {
        supplier.setId(id);
        supplierMapper.updateById(supplier);
        return Result.success(supplier);
    }

    public Result<?> delete(Integer id) {
        supplierMapper.deleteById(id);
        return Result.success();
    }
}