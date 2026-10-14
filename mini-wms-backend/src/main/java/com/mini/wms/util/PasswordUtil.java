package com.mini.wms.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * 密码工具类
 * 使用 SHA-256 对密码进行哈希处理
 */
public class PasswordUtil {

    /**
     * 密码哈希
     * @param plainText 原始密码
     * @return 哈希后的密码
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码哈希失败", e);
        }
    }
}
