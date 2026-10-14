package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.StockInItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单明细 Mapper
 */
@Mapper
public interface StockInItemMapper extends BaseMapper<StockInItem> {
}