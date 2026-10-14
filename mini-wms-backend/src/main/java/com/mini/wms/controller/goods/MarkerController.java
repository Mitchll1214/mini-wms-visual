package com.mini.wms.controller.goods;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.ImageMarker;
import com.mini.wms.service.MarkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标记控制器
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class MarkerController {

    private final MarkerService markerService;

    /**
     * 获取图片标记列表
     */
    @GetMapping("/{imageId}/markers")
    public Result<?> list(@PathVariable Integer imageId) {
        return markerService.listByImage(imageId);
    }

    /**
     * 保存标记
     */
    @PostMapping("/{imageId}/markers")
    public Result<?> save(@PathVariable Integer imageId,
                          @RequestBody List<ImageMarker> markers) {
        return markerService.save(imageId, markers);
    }
}