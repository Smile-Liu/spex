package org.spex.beans.factory.config;

import org.spex.beans.factory.MutablePropertyValues;

public interface BeanDefinition {

	String getBeanClassName();
	
	void setBeanClassName(String beanClassName);
	
	String getFactoryBeanName();
	
	void setFactoryBeanName(String factoryBeanName);
	
	MutablePropertyValues getPropertyValues();

	void setPropertyValues(MutablePropertyValues propertyValues);
	
	boolean isAutowireCandidate();
}
