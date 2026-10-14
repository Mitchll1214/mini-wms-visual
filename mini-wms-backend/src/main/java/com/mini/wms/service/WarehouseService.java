package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Warehouse;

/**
 * 仓库服务接口
 */
public interface WarehouseService {

    Result<?> list();

    Result<?> get(Integer id);

    Result<?> create(Warehouse warehouse);

    Result<?> update(Integer id, Warehouse warehouse);

    Result<?> delete(Integer id);
}