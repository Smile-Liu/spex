package org.spex.beans;

public class NotWritablePropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 1L;

	public NotWritablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "属性'" + propertyName + "'是不可写的或没有有效的setter方法" +
				"请确定getter的返回值类型是否匹配setter的参数类型");
	}
	
	public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}
}
