package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Bin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 储位 Mapper
 */
@Mapper
public interface BinMapper extends BaseMapper<Bin> {

    /**
     * 根据货架 ID 查询储位
     */
    @Select("SELECT * FROM bin WHERE shelf_id = #{shelfId} ORDER BY code")
    List<Bin> selectByShelfId(@Param("shelfId") Integer shelfId);

    /**
     * 根据储位编码查询储位
     */
    @Select("SELECT * FROM bin WHERE code = #{code} LIMIT 1")
    Bin selectByCode(@Param("code") String code);

    /**
     * 查询全部储位并附带仓库/库区/货架层级名称（下拉展示用）
     */
    @Select("SELECT b.id, b.shelf_id AS shelfId, b.code, b.name, b.status, " +
            "s.name AS shelfName, a.name AS areaName, w.name AS warehouseName " +
            "FROM bin b " +
            "LEFT JOIN shelf s ON s.id = b.shelf_id " +
            "LEFT JOIN warehouse_area a ON a.id = s.area_id " +
            "LEFT JOIN warehouse w ON w.id = a.warehouse_id " +
            "ORDER BY w.name, a.name, s.name, b.code")
    List<Map<String, Object>> selectAllWithLocation();
}