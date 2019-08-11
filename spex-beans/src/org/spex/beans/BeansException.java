package org.spex.beans;

public class BeansException extends RuntimeException {

	private static final long serialVersionUID = 5253763060200612732L;
	
	public BeansException(String msg) {
		super(msg);
	}
	
	public BeansException(String msg, Throwable e) {
		super(msg, e);
	}

}
