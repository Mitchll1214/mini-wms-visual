package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.ImageMarker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 图片标记 Mapper
 */
@Mapper
public interface ImageMarkerMapper extends BaseMapper<ImageMarker> {

    /**
     * 根据图片 ID 查询标记列表
     */
    @Select("SELECT * FROM image_marker WHERE image_id = #{imageId} AND deleted_at IS NULL")
    List<ImageMarker> selectByImageId(@Param("imageId") Integer imageId);

    /**
     * 根据图片 ID 删除标记
     */
    @Delete("DELETE FROM image_marker WHERE image_id = #{imageId}")
    int deleteByImageId(@Param("imageId") Integer imageId);
}