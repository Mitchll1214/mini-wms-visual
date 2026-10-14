package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.GoodsImage;
import com.mini.wms.mapper.ImageMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片服务
 */
@Service
public class ImageService {

    private final ImageMapper imageMapper;

    public ImageService(ImageMapper imageMapper) {
        this.imageMapper = imageMapper;
    }

    public Result<?> listByGoods(Integer goodsId, String imageType) {
        return Result.success(imageMapper.selectByGoodsId(goodsId, imageType));
    }

    public Result<?> countByGoods(Integer goodsId, String imageType) {
        return Result.success(imageMapper.countByGoodsId(goodsId, imageType));
    }

    /**
     * 管理员图片管理：分页列表
     */
    public Result<?> adminPage(int page, int size, String imageType) {
        long total = imageMapper.countAllForAdmin(imageType);
        List<Map<String, Object>> records = imageMapper.pageAllForAdmin(imageType,
                (page - 1) * size, size);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }
}