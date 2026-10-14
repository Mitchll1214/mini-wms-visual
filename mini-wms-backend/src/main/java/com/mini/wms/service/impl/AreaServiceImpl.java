package com.mini.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.WarehouseArea;
import com.mini.wms.mapper.AreaMapper;
import com.mini.wms.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl extends ServiceImpl<AreaMapper, WarehouseArea> implements AreaService {

    private final AreaMapper areaMapper;

    @Override
    public Result<?> listByWarehouse(Integer warehouseId) {
        return Result.success(areaMapper.selectByWarehouseId(warehouseId));
    }

    @Override
    public Result<?> get(Integer id) {
        return Result.success(areaMapper.selectById(id));
    }

    @Override
    public Result<?> create(WarehouseArea area) {
        area.setCreatedAt(java.time.LocalDateTime.now());
        areaMapper.insert(area);
        return Result.success(area);
    }

    @Override
    public Result<?> update(Integer id, WarehouseArea area) {
        area.setId(id);
        areaMapper.updateById(area);
        return Result.success(area);
    }

    @Override
    public Result<?> delete(Integer id) {
        areaMapper.deleteById(id);
        return Result.success();
    }
}