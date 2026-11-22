package com.lumos.common;

import java.io.Serializable;

// 统一响应结果
public class R<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> R<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
