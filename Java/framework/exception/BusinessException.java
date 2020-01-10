package com.emotte.cms.core.framework.exception;

import com.emotte.common.utils.enums.CodeEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author
 * @description: 公共异常处理类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String msg;
	private String code = "500";
	private Object data;

	public BusinessException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public BusinessException(CodeEnum codeEnum) {
		super(codeEnum.getMsg());
		this.msg = codeEnum.getMsg();
		this.code = codeEnum.getCode();
	}

	public BusinessException(CodeEnum codeEnum, String msg) {
		super("【" + codeEnum.getCode() + "】" + codeEnum.getMsg() + " " + msg);
		this.msg = codeEnum.getMsg() + " " + msg;
		this.code = codeEnum.getCode();
	}

	public BusinessException(String code, String msg) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public BusinessException(String code, String msg, Object data) {
		super(msg);
		this.msg = msg;
		this.code = code;
		this.data = data;
	}

	public BusinessException(String code, String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

}
