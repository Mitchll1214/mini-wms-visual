package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.SupplierImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 供应商资质图片 Mapper
 */
@Mapper
public interface SupplierImageMapper extends BaseMapper<SupplierImage> {

    @Select("SELECT * FROM supplier_image WHERE supplier_id = #{supplierId} AND deleted_at IS NULL ORDER BY id DESC")
    List<SupplierImage> selectBySupplierId(@Param("supplierId") Integer supplierId);

    @Select("SELECT * FROM supplier_image WHERE deleted_at IS NULL AND relative_path LIKE 'supplier/%' ORDER BY id DESC")
    List<SupplierImage> selectAll();
}