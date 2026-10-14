package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.WarehouseArea;

import java.util.List;

/**
 * 库区服务接口
 */
public interface AreaService {

    Result<?> listByWarehouse(Integer warehouseId);

    Result<?> get(Integer id);

    Result<?> create(WarehouseArea area);

    Result<?> update(Integer id, WarehouseArea area);

    Result<?> delete(Integer id);
}