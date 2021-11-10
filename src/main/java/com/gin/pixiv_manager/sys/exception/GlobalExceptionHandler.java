package com.gin.pixiv_manager.sys.exception;

import com.gin.pixiv_manager.sys.response.Res;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author bx002
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 参数校验异常
     * @param exception 异常
     * @return 报错信息
     */
    @ResponseBody
    @ExceptionHandler({BindException.class, ConstraintViolationException.class, MethodArgumentNotValidException.class})
    public Res<Set<String>> handleViolationException(Exception exception) {
        Set<String> data = new HashSet<>();
        if (exception instanceof BindException) {
            BindException e = (BindException) exception;
            e.getAllErrors().forEach(i -> data.add(i.getDefaultMessage()));
        }
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException e = (ConstraintViolationException) exception;
            e.getConstraintViolations().forEach(i -> data.add(i.getMessage()));
        }
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException e = (MethodArgumentNotValidException) exception;
            e.getBindingResult().getAllErrors().forEach(i -> data.add(i.getDefaultMessage()));
        }

        return Res.fail(3000, "参数校验错误", data);
    }



    @ResponseBody
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public Res<String> shiroExceptionHandler(HttpMessageNotReadableException e) {
        return Res.fail(3000, "参数校验错误", "缺少request body");
    }


    @ResponseBody
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public Res<Void> handler(HttpRequestMethodNotSupportedException e) {
        return Res.fail(9000, "不支持的请求方法：" + e.getLocalizedMessage());
    }

    @ResponseBody
    @ExceptionHandler({BusinessException.class})
    public Res<Void> handler(BusinessException e) {
        log.warn("{} {}", e.getCode(), e.getMessage());
        return Res.fail(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler({UnexpectedRollbackException.class})
    public Res<Void> handler(UnexpectedRollbackException e) {
        log.warn("{} ", e.getMessage());
        return Res.fail(9999, "未知错误 请报告管理员");
    }

    @ResponseBody
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public Res<Void> handler(MissingServletRequestParameterException e) {
        e.printStackTrace();
        return Res.fail(3000, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler({Exception.class})
    public Res<Void> handler(Exception e) {
        e.printStackTrace();
        return Res.fail(9999, "未知错误 请报告管理员");
    }


}
