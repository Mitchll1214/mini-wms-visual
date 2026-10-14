package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.Shelf;
import com.mini.wms.mapper.ShelfMapper;
import com.mini.wms.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 货架服务实现
 */
@Service
@RequiredArgsConstructor
public class ShelfServiceImpl implements ShelfService {

    private final ShelfMapper shelfMapper;

    @Override
    public Result<?> listByArea(Integer areaId) {
        return Result.success(shelfMapper.selectByAreaId(areaId));
    }

    @Override
    public Result<?> get(Integer id) {
        return Result.success(shelfMapper.selectById(id));
    }

    @Override
    public Result<?> create(Shelf shelf) {
        shelf.setCreatedAt(java.time.LocalDateTime.now());
        shelfMapper.insert(shelf);
        return Result.success(shelf);
    }

    @Override
    public Result<?> update(Integer id, Shelf shelf) {
        shelf.setId(id);
        shelfMapper.updateById(shelf);
        return Result.success(shelf);
    }

    @Override
    public Result<?> delete(Integer id) {
        shelfMapper.deleteById(id);
        return Result.success();
    }
}