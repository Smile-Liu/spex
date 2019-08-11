package org.spex.beans;

public class NotReadablePropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 1L;

	public NotReadablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "属性'" + propertyName + "'是不可读的或没有有效的getter方法" +
				"请确定getter的返回值类型是否匹配setter的参数类型");
	}
	
	public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}
}
