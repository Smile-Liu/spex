package org.spex.beans.factory.config;

import java.beans.PropertyDescriptor;

import org.spex.beans.PropertyValues;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	Object postProcessBeforeInstantiation(Class<?> bean, String beanName);
	
	boolean postProcessAfterInstantiation(Object bean, String beanName);
	
	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName);
}
