package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库 Mapper
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
    // 继承了 BaseMapper 的基本 CRUD 方法
}