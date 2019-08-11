package org.spex.beans.factory;

public class FactoryBeanNotInitializedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FactoryBeanNotInitializedException() {
		super("FactoryBean��û��ʵ�������");
	}

	public FactoryBeanNotInitializedException(String message) {
		super(message);
	}

	
}
