package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商 Mapper
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
    // 继承了 BaseMapper 的基本 CRUD 方法
}