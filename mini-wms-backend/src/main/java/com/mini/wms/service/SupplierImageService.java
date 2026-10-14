package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.mapper.SupplierImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 供应商资质图片服务
 */
@Service
@RequiredArgsConstructor
public class SupplierImageService {

    private final SupplierImageMapper supplierImageMapper;

    /**
     * 某供应商的资质图片列表
     */
    public Result<?> listBySupplier(Integer supplierId) {
        return Result.success(supplierImageMapper.selectBySupplierId(supplierId));
    }
}