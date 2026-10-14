package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.ImageMarker;
import com.mini.wms.mapper.ImageMarkerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标记服务
 */
@Service
public class MarkerService {

    private final ImageMarkerMapper markerMapper;

    public MarkerService(ImageMarkerMapper markerMapper) {
        this.markerMapper = markerMapper;
    }

    public Result<?> listByImage(Integer imageId) {
        return Result.success(markerMapper.selectByImageId(imageId));
    }

    public Result<?> save(Integer imageId, List<ImageMarker> markers) {
        // 先删除旧标记
        markerMapper.deleteByImageId(imageId);

        // 保存新标记
        for (ImageMarker marker : markers) {
            markerMapper.insert(marker);
        }

        return Result.success();
    }
}