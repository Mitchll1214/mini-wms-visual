package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品 Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 根据商品编码查询商品
     */
    @Select("SELECT * FROM goods WHERE code = #{code} LIMIT 1")
    Goods selectByCode(@Param("code") String code);

    /**
     * 根据条形码查询商品
     */
    @Select("SELECT * FROM goods WHERE barcode = #{barcode} LIMIT 1")
    Goods selectByBarcode(@Param("barcode") String barcode);

    /**
     * 批量查询商品
     */
    @Select("<script>" +
            "SELECT * FROM goods WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Goods> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 更新商品更新时间
     */
    @Update("UPDATE goods SET updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateUpdatedAt(@Param("id") Integer id);
}