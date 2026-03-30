package com.server.utils;

import lombok.Data;
import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> success() {
        return new R<>(200, "success", null);
    }

    public static <T> R<T> success(String message) {
        return new R<>(200, message, null);
    }

    public static <T> R<T> success(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }

    public static <T> R<T> error() {
        return new R<>(500, "error", null);
    }

    public static <T> R<T> error(String message) {
        return new R<>(500, message, null);
    }

    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> error(int code, String message, T data) {
        return new R<>(code, message, data);
    }
}
