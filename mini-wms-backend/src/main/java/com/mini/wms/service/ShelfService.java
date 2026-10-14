package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Shelf;

import java.util.List;

/**
 * 货架服务接口
 */
public interface ShelfService {

    Result<?> listByArea(Integer areaId);

    Result<?> get(Integer id);

    Result<?> create(Shelf shelf);

    Result<?> update(Integer id, Shelf shelf);

    Result<?> delete(Integer id);
}