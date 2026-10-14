package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.GoodsImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品图片 Mapper
 */
@Mapper
public interface ImageMapper extends BaseMapper<GoodsImage> {

    /**
     * 根据商品 ID 查询图片列表
     */
    @Select("SELECT * FROM goods_image WHERE goods_id = #{goodsId} AND image_type = #{imageType} AND deleted_at IS NULL ORDER BY id DESC")
    List<GoodsImage> selectByGoodsId(@Param("goodsId") Integer goodsId, @Param("imageType") String imageType);

    /**
     * 批量查询多个商品的主图（返回图片 id 与缩略图路径）
     */
    @Select("<script>" +
            "SELECT gi.id, gi.goods_id, gi.thumb_path FROM goods_image gi " +
            "WHERE gi.image_type = 'main' AND gi.deleted_at IS NULL AND gi.goods_id IN " +
            "<foreach collection='goodsIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            " ORDER BY gi.id DESC</script>")
    List<Map<String, Object>> selectMainThumbs(@Param("goodsIds") List<Integer> goodsIds);

    /**
     * 根据商品 ID 查询图片数量
     */
    @Select("SELECT COUNT(*) FROM goods_image WHERE goods_id = #{goodsId} AND image_type = #{imageType} AND deleted_at IS NULL")
    int countByGoodsId(@Param("goodsId") Integer goodsId, @Param("imageType") String imageType);

    /**
     * 管理员图片管理：全部图片数量（goods_image + supplier_image）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM (" +
            " SELECT id FROM goods_image WHERE deleted_at IS NULL" +
            " UNION ALL" +
            " SELECT id FROM supplier_image WHERE deleted_at IS NULL" +
            ") t " +
            "<if test='imageType != null and imageType != \"\"'>" +
            " WHERE 1 = 1" +
            "</if></script>")
    long countAllForAdmin(@Param("imageType") String imageType);

    /**
     * 管理员图片管理：分页列表（联合 goods_image + supplier_image）
     * source: goods / supplier
     */
    @Select("<script>" +
            "SELECT * FROM (" +
            "  SELECT 'goods' AS source, gi.id, gi.image_type, gi.goods_id AS biz_id, g.name AS biz_name," +
            "         gi.relative_path, gi.thumb_path, gi.mime_type, gi.file_size, gi.created_at" +
            "  FROM goods_image gi LEFT JOIN goods g ON g.id = gi.goods_id" +
            "  WHERE gi.deleted_at IS NULL" +
            "  UNION ALL" +
            "  SELECT 'supplier' AS source, si.id, si.image_type, si.supplier_id AS biz_id, s.name AS biz_name," +
            "         si.relative_path, si.thumb_path, si.mime_type, si.file_size, si.created_at" +
            "  FROM supplier_image si LEFT JOIN supplier s ON s.id = si.supplier_id" +
            "  WHERE si.deleted_at IS NULL" +
            ") all_images" +
            " <if test='imageType != null and imageType != \"\"'>WHERE image_type = #{imageType}</if>" +
            " ORDER BY created_at DESC" +
            " LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> pageAllForAdmin(@Param("imageType") String imageType,
                                              @Param("offset") int offset,
                                              @Param("size") int size);
}