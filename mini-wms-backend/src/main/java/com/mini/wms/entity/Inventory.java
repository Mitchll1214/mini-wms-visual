package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存实体（商品 + 储位维度）
 */
@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer binId;

    private Integer qty;

    private LocalDateTime deletedAt;

    private LocalDateTime deletedExpireAt;

    private LocalDateTime updatedAt;
}