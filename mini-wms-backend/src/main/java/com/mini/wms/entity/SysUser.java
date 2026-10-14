package com.mini.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String passwordHash;

    /** 角色：ADMIN / OPERATOR / INSPECTOR */
    private String role;

    /** 是否启用：1 启用 / 0 禁用 */
    private Integer enabled;

    private LocalDateTime createdAt;
}