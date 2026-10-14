package com.mini.wms.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    private String clientType; // PC / MOBILE
    private String deviceInfo; // 设备信息
}