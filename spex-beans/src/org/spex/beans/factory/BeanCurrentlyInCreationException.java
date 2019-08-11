package org.spex.beans.factory;

public class BeanCurrentlyInCreationException extends BeanCreationException {

	private static final long serialVersionUID = 1L;

	public BeanCurrentlyInCreationException(String beanName, Throwable cause) {
		super(beanName + " 正在创建中，请查看是否有循环依赖", cause);
	}

	public BeanCurrentlyInCreationException(String beanName, String msg) {
		super(beanName + " " + msg);
	}
	
}
