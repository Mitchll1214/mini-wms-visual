package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水 Mapper
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}