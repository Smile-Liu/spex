package org.spex.beans.factory;

public class FactoryBeanNotInitializedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FactoryBeanNotInitializedException() {
		super("FactoryBean还没有实例化完成");
	}

	public FactoryBeanNotInitializedException(String message) {
		super(message);
	}

	
}
