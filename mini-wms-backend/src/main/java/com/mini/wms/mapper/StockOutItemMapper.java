package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.StockOutItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单明细 Mapper
 */
@Mapper
public interface StockOutItemMapper extends BaseMapper<StockOutItem> {
}