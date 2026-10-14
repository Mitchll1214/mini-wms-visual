package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.WarehouseArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库区 Mapper
 */
@Mapper
public interface AreaMapper extends BaseMapper<WarehouseArea> {

    /**
     * 根据仓库 ID 查询库区列表
     */
    @Select("SELECT * FROM warehouse_area WHERE warehouse_id = #{warehouseId}")
    List<WarehouseArea> selectByWarehouseId(@Param("warehouseId") Integer warehouseId);
}