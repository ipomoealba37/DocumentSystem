package com.javaee.documentsystem.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e){
        //打印错误日记
        e.printStackTrace();
        return Result.error(500,"系统异常，请联系管理员");
    }
}
