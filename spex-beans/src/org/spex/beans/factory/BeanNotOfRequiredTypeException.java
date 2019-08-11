package org.spex.beans.factory;

import org.spex.beans.BeansException;

public class BeanNotOfRequiredTypeException extends BeansException {

	private static final long serialVersionUID = 1L;

	private String beanName;
	
	private Class<?> requireType;
	
	private Class<?> actualType;
	
	public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
		super("Bean " + beanName + " 要求类型为：" + requiredType.getName() + "，实际类型为：" + actualType.getName());
		this.beanName = beanName;
		this.requireType = requiredType;
		this.actualType = actualType;
	}

	public String getBeanName() {
		return beanName;
	}

	public Class<?> getRequireType() {
		return requireType;
	}

	public Class<?> getActualType() {
		return actualType;
	}

}
