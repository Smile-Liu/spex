package org.spex.beans.factory.support;

public class BeanDefinitionValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}
	
	public BeanDefinitionValidationException(String msg, Throwable e) {
		super(msg, e);
	}

}
