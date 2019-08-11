package org.spex.beans.factory;

public class CannotLoadBeanClassException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CannotLoadBeanClassException() {
		super();
	}
	
	public CannotLoadBeanClassException(String beanName, String beanClassName, ClassNotFoundException e) {
		super("创建Bean " + beanName + " 实例时找不到Class " + beanClassName, e);
	}
	
}
