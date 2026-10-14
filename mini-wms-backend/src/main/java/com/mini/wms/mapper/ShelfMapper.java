package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Shelf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 货架 Mapper
 */
@Mapper
public interface ShelfMapper extends BaseMapper<Shelf> {

    /**
     * 根据库区 ID 查询货架列表
     */
    @Select("SELECT * FROM shelf WHERE area_id = #{areaId}")
    List<Shelf> selectByAreaId(@Param("areaId") Integer areaId);
}