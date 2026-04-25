package com.mssj.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Result {

    private Integer code;
    private String message;
    private Object data;
    private String time;

    private Result() {
        this.time= LocalDateTime.now().toString();
    }

    // 成功

    public static Result success() {
        Result r = new Result();
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static Result success(Object data) {
        Result r = new Result();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static Result success(String message, Object data) {
        Result r = new Result();
        r.code = 200;
        r.message = message;
        r.data = data;
        return r;
    }

    //失败

    public static Result fail(String message) {
        Result r = new Result();
        r.code = 500;
        r.message = message;
        return r;
    }

    public static Result fail(Integer code, String message) {
        Result r = new Result();
        r.code = code;
        r.message = message;
        return r;
    }
}