package com.mini.wms.service.impl;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Warehouse;
import com.mini.wms.mapper.WarehouseMapper;
import com.mini.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 仓库服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Result<?> list() {
        return Result.success(warehouseMapper.selectList(null));
    }

    @Override
    public Result<?> get(Integer id) {
        return Result.success(warehouseMapper.selectById(id));
    }

    @Override
    public Result<?> create(Warehouse warehouse) {
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouseMapper.insert(warehouse);
        return Result.success(warehouse);
    }

    @Override
    public Result<?> update(Integer id, Warehouse warehouse) {
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
        return Result.success(warehouse);
    }

    @Override
    public Result<?> delete(Integer id) {
        warehouseMapper.deleteById(id);
        return Result.success();
    }
}