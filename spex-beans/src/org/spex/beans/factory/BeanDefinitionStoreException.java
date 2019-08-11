package org.spex.beans.factory;

public class BeanDefinitionStoreException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private String beanName;

	public BeanDefinitionStoreException(String msg) {
		super(msg);
	}
	
	public BeanDefinitionStoreException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public BeanDefinitionStoreException(String msg, Throwable e, String beanName) {
		super(beanName + "∑«∑®≈‰÷√:" + msg, e);
		this.beanName = beanName;
		
	}

	public String getBeanName() {
		return beanName;
	}
	
}
