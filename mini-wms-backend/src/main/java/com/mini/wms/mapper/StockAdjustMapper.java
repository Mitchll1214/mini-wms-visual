package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.StockAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存调整 Mapper（复用 stock_log 表）
 */
@Mapper
public interface StockAdjustMapper extends BaseMapper<StockAdjustment> {

    /**
     * 根据商品ID和储位ID查询库存调整记录（取最新一条）
     */
    @Select("SELECT * FROM stock_log WHERE goods_id = #{goodsId} AND bin_id = #{binId} AND change_type = 'adjust' ORDER BY created_at DESC LIMIT 1")
    StockAdjustment selectByGoodsAndBin(@Param("goodsId") Integer goodsId, @Param("binId") Integer binId);
}