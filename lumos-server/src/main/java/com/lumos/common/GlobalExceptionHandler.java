package com.lumos.common;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<Void> handleValid(Exception e) {
        return R.error(400, "参数校验失败：" + e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.error(400, "请求体格式错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleUpload(MaxUploadSizeExceededException e) {
        return R.error(400, "文件大小超出限制");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e) {
        e.printStackTrace();
        return R.error("系统异常：" + e.getMessage());
    }
}
