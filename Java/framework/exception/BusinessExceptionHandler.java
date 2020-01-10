package com.emotte.cms.core.framework.exception;

import com.emotte.common.utils.constant.BusinessStatus;
import com.emotte.common.utils.response.BaseResponse;
import com.emotte.common.utils.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * @author
 * @description: CommonExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class BusinessExceptionHandler {

    /**
     * 自定义异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse handleRRException(BusinessException e) {
        return ResultUtils.fail(e.getCode(), e.getMsg());
    }

    /**
     * 处理必要参数未传递的handler
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public BaseResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("必要参数未传递", e);
        return ResultUtils.fail(BusinessStatus.PARAM_ILLEGAL, "必要参数未传递");
    }

    /**
     * 请求内容解析错误的handler
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("解析参数错误，参数可能未传递", e);
        return ResultUtils.fail(BusinessStatus.PARAM_ILLEGAL, "解析参数错误，参数可能未传递");
    }

    /**
     * 处理参数校验失败的异常handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final BindingResult bindingResult = e.getBindingResult();
        final List<FieldError> errors = bindingResult.getFieldErrors();
        final int size = errors.size();
        final StringBuilder sb = new StringBuilder();
        FieldError error;
        for (int i = 0; i < size; i++) {
            error = errors.get(i);
            sb.append(error.getField()).append(": ").append(error.getDefaultMessage());
            if (i != size - 1) {
                sb.append(", ");
            }
        }
        final String msg = sb.toString();
        log.error("参数校验未通过: " + msg, e);
        return ResultUtils.fail(BusinessStatus.PARAM_ILLEGAL, msg);
    }

    /**
     * 处理路径不存在的handler
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseResponse handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("路径不存在，请检查路径是否正确", e);
        return ResultUtils.fail(String.valueOf(HttpStatus.NOT_FOUND.value()), "路径不存在，请检查路径是否正确");
    }

    /**
     * 处理所有异常的handler，保证所有返回的数据结构一致
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse handleException(Exception e) {
        log.error("内部错误", e);
        return ResultUtils.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage());
    }

    /**
     * 处理数据库主键或者约束条件已存在的handler
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public BaseResponse handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据已存在", e);
        return ResultUtils.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "数据已存在");
    }
}
