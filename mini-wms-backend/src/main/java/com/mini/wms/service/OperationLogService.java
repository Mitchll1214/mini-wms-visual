package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.OperationLog;
import com.mini.wms.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志（按时间倒序，支持按模块关键字筛选）
     */
    public Result<?> page(int page, int size, String module) {
        Page<OperationLog> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .like(module != null && !module.isEmpty(), OperationLog::getModule, module)
                .orderByDesc(OperationLog::getCreatedAt)
                .orderByDesc(OperationLog::getId);
        IPage<OperationLog> result = operationLogMapper.selectPage(pageObj, wrapper);
        return Result.success(result);
    }

    /**
     * 记录操作日志
     */
    public void record(String module, String action, String detail, String operator, String deviceInfo) {
        OperationLog log = new OperationLog();
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setOperator(operator);
        log.setDeviceInfo(deviceInfo);
        log.setCreatedAt(java.time.LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}