package org.spex.beans.factory.config;

public class BeanDefinitionHolder {

	private final BeanDefinition beanDefinition;
	
	private final String beanName;

	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String getBeanName() {
		return beanName;
	}
	
}
