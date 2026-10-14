package com.mini.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mini.wms.entity.RecycleBin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 回收站 Mapper
 */
@Mapper
public interface RecycleBinMapper extends BaseMapper<RecycleBin> {

    /**
     * 根据业务类型和 ID 查询回收站记录
     */
    @Select("SELECT * FROM recycle_bin WHERE biz_type = #{bizType} AND biz_id = #{bizId}")
    RecycleBin selectByBiz(@Param("bizType") String bizType, @Param("bizId") Integer bizId);

    /**
     * 获取待清理的过期记录
     */
    @Select("SELECT * FROM recycle_bin WHERE expire_at <= datetime('now', 'localtime') AND restored = 0")
    List<RecycleBin> selectExpiredRecords();
}