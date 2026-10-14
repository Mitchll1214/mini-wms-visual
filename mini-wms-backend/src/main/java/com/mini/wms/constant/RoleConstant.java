package com.mini.wms.constant;

/**
 * 角色常量
 * 管理员 / 操作员 / 盘点员
 */
public final class RoleConstant {

    /** 管理员：拥有所有权限 */
    public static final String ADMIN = "ADMIN";

    /** 操作员：除用户管理和删除数据以外的权限 */
    public static final String OPERATOR = "OPERATOR";

    /** 盘点员：仅查看权限 */
    public static final String INSPECTOR = "INSPECTOR";

    private RoleConstant() {}
}