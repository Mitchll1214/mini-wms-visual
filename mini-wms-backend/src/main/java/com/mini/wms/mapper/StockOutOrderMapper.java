package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.entity.StockOutOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 出库单 Mapper
 */
@Mapper
public interface StockOutOrderMapper extends BaseMapper<StockOutOrder> {

    /**
     * 分页查询出库单
     */
    @Select("<script>" +
            "SELECT * FROM stock_out_order " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>AND (order_no LIKE '%' || #{keyword} || '%' OR operator LIKE '%' || #{keyword} || '%')</if>" +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<StockOutOrder> selectPage(Page<StockOutOrder> page,
                                    @Param("keyword") String keyword,
                                    @Param("status") String status);
}