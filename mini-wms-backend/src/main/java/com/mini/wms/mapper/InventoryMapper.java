package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 库存 Mapper
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 根据商品 ID 查询库存明细
     */
    @Select("SELECT * FROM inventory WHERE goods_id = #{goodsId} AND deleted_at IS NULL")
    List<Inventory> selectByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 根据储位 ID 查询库存明细
     */
    @Select("SELECT * FROM inventory WHERE bin_id = #{binId} AND deleted_at IS NULL")
    List<Inventory> selectByBinId(@Param("binId") Integer binId);

    /**
     * 根据商品 ID 查询有库存的储位列表（含储位编码，出库下拉用）
     */
    @Select("SELECT i.bin_id AS binId, i.qty, b.code AS binCode, b.name AS binName, " +
            "sh.name AS shelfName, ar.name AS areaName, wh.name AS warehouseName " +
            "FROM inventory i LEFT JOIN bin b ON i.bin_id = b.id " +
            "LEFT JOIN shelf sh ON sh.id = b.shelf_id " +
            "LEFT JOIN warehouse_area ar ON ar.id = sh.area_id " +
            "LEFT JOIN warehouse wh ON wh.id = ar.warehouse_id " +
            "WHERE i.goods_id = #{goodsId} AND i.deleted_at IS NULL AND i.qty > 0 " +
            "ORDER BY i.qty DESC")
    List<Map<String, Object>> selectBinsByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 根据商品 ID 计算总库存
     */
    @Select("SELECT COALESCE(SUM(qty), 0) FROM inventory WHERE goods_id = #{goodsId} AND deleted_at IS NULL")
    int selectTotalByGoodsId(@Param("goodsId") Integer goodsId);
}