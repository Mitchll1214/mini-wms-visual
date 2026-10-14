package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Bin;

import java.util.List;

/**
 * 储位服务接口
 */
public interface BinService {

    Result<?> listByShelf(Integer shelfId);

    Result<?> tree();

    Result<?> get(Integer id);

    Result<?> create(Bin bin);

    Result<?> update(Integer id, Bin bin);

    Result<?> delete(Integer id);

    Result<?> qrcode(Integer id);
}