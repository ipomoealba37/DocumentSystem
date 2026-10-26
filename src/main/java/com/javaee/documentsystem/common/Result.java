package com.javaee.documentsystem.common;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;
    private T data;

    public Result() {}
    public Result(Integer code,String msg,T data){
        this.code=code;
        this.msg=msg;
        this.data=data;
    }

    //快速返回成功（有数据）
    public static <T> Result<T> success(T data){
        return new Result<>(200,"操作成功",data);
    }


    //快速返回成功（无数据）
    public static <T> Result<T> success(){
        return new Result<>(200,"操作成功",null);
    }


    //快速返回失败
    public static <T> Result<T> error(String msg){
        return new Result<>(500,msg,null);
    }


    //自定义状态码失败
    public static <T> Result<T> error(Integer code,String msg){
        return new Result<>(code,msg,null);
    }

    //getter and setter

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
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
