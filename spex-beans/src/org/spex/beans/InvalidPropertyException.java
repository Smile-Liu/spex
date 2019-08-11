package org.spex.beans;

public class InvalidPropertyException extends BeansException {

	private static final long serialVersionUID = 1L;

	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg) {
		this(beanClass, propertyName, msg, null);
	}

	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg, Throwable e) {
		super("Bean[" + beanClass.getName() + "]的非法属性'" + propertyName + "'，" + msg, e);
	}
}
