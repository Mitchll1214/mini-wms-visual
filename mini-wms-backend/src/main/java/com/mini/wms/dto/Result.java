package com.mini.wms.dto;

import lombok.Data;

/**
 * 统一返回格式
 */
@Data
public class Result<T> {

    private int code;

    private String message;

    private T data;

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}