package com.mini.wms.controller.stock;

import com.mini.wms.dto.Result;
import com.mini.wms.dto.InventoryAdjustmentRequest;
import com.mini.wms.service.StockAdjustService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存调整控制器
 * 仅主程序电脑可见，手机端拦截
 */
@RestController
@RequestMapping("/api/inventory/adjust")
@RequiredArgsConstructor
public class StockAdjustController {

    private final StockAdjustService stockAdjustService;

    /**
     * 库存调整（仅 PC 可用）
     * @param request 调整请求
     * @return 操作结果
     */
    @PostMapping
    public Result<?> adjust(@RequestBody InventoryAdjustmentRequest request) {
        // 验证是否为主程序电脑操作
        Result<?> validateResult = stockAdjustService.validatePcOperation(request.getDeviceInfo());
        if (validateResult.getCode() != 0) {
            return validateResult;
        }

        return stockAdjustService.adjust(request);
    }
}