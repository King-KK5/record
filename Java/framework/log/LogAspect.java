package com.emotte.cms.core.framework.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @program: cms-core
 * @description: 日志处理切面，处理被com.emotte.hss.core.framework.log.RequestLog注解标记的方法
 * @author: Mr.Wang
 * @date: 2019-11-27 13:55
 **/
@Slf4j
@Aspect
@Component
public class LogAspect {

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 切点
	 */
	@Pointcut("@annotation(com.emotte.cms.core.framework.log.Log)")
	public void logPointCut() {
	}

	/**
	 * 方法环绕切面
	 */
	@SuppressWarnings("rawtypes")
	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		final Signature signature = joinPoint.getSignature();
		final Method method = ((MethodSignature) signature).getMethod();
		final Log annotation = method.getAnnotation(Log.class);
		final Class[] exclude = annotation.exclude();
		final String value = annotation.value();
		final String className = joinPoint.getTarget().getClass().getName();
		final String methodName = signature.getName();
		final Object[] args = joinPoint.getArgs();
		final String[] parameterNames = ((MethodSignature) signature).getParameterNames();
		final Class[] parameterTypes = ((MethodSignature) signature).getParameterTypes();
		final StringBuilder sb = new StringBuilder();
		final String lineSeparator = System.getProperty("line.separator");
		sb.append(lineSeparator).append("====================================>");
		if (StringUtils.isNotBlank(value)) {
			sb.append(lineSeparator).append("操作内容: ").append(value);
		}
		sb.append(lineSeparator).append("执行方法: ").append(className).append("#").append(methodName).append("(");
		final int length = parameterTypes.length;
		for (int i = 0; i < length; i++) {
			sb.append(parameterTypes[i].getName());
			if (i != length - 1) {
				sb.append(", ");
			}
		}
		sb.append(")");
		if (length > 0) {
			sb.append(lineSeparator).append("入参内容: ");
			final boolean hasExclude = !ArrayUtils.isEmpty(exclude);
			Class clazz;
			for (int i = 0; i < length; i++) {
				clazz = parameterTypes[i];
				if (hasExclude && inExclude(clazz, exclude)) {
					continue;
				}
				sb.append(parameterNames[i]).append(": ").append(objectMapper.writeValueAsString(args[i]));
				if (i != length - 1) {
					sb.append(", ");
				}
			}
		}
		try {
			final long before = System.currentTimeMillis();
			final Object proceed = joinPoint.proceed();
			final long after = System.currentTimeMillis();
			sb.append(lineSeparator).append("返回类型: ").append(ResolvableType.forMethodReturnType(method));
			sb.append(lineSeparator).append("返回内容: ").append(objectMapper.writeValueAsString(proceed));
			sb.append(lineSeparator).append("执行耗时: ").append(after - before).append("ms");
			sb.append(lineSeparator).append("<====================================");
			log.info(sb.toString());
			return proceed;
		} catch (Exception e) {
			sb.append(lineSeparator).append("发生错误: ").append(e.getMessage());
			sb.append(lineSeparator).append("<====================================");
			// ExceptionHandler会打印一次错误堆栈，所以这里不需要打印
			log.error(sb.toString());
			throw e;
		}
	}

	/**
	 * 校验当前参数类型是否在排除列表内
	 */
	@SuppressWarnings("rawtypes")
	private boolean inExclude(Class type, Class[] exclude) {
		for (Class clazz : exclude) {
			if (clazz == type) {
				return true;
			}
		}
		return false;
	}
}
