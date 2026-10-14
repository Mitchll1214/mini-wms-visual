package com.mini.wms.controller.recycle;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.RecycleBin;
import com.mini.wms.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回收站控制器
 */
@RestController
@RequestMapping("/api/recycle")
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 获取回收站列表
     */
    @GetMapping("/page")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String bizType) {
        return recycleBinService.list(page, size, bizType);
    }

    /**
     * 处理零库存删除（仅当库存为0时可用）
     */
    @PostMapping("/delete-zero-stock")
    public Result<?> deleteZeroStock(@RequestParam Integer goodsId,
                                     @RequestParam Integer binId,
                                     @RequestParam String operator,
                                     @RequestParam String deviceInfo) {
        return recycleBinService.deleteZeroStock(goodsId, binId, operator, deviceInfo);
    }

    /**
     * 恢复回收站记录
     */
    @PostMapping("/{id}/restore")
    public Result<?> restore(@PathVariable Integer id) {
        return recycleBinService.restore(id);
    }

    /**
     * 系统启动时清理过期记录（内部调用）
     */
    @PostMapping("/cleanup")
    public Result<?> cleanupExpired() {
        recycleBinService.cleanupExpired();
        return Result.success("过期记录清理完成");
    }
}