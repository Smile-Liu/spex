package org.spex.beans.factory;

public class BeanCurrentlyInCreationException extends BeanCreationException {

	private static final long serialVersionUID = 1L;

	public BeanCurrentlyInCreationException(String beanName, Throwable cause) {
		super(beanName + " ���ڴ����У���鿴�Ƿ���ѭ������", cause);
	}

	public BeanCurrentlyInCreationException(String beanName, String msg) {
		super(beanName + " " + msg);
	}
	
}
