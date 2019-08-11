package org.spex.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.spex.beans.factory.BeanFactory;

public interface InstantiationStrategy {

	
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner);
	
	
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, 
			Object factoryBean, Method factoryMethod, Object[] args);
	
	
	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, 
			Constructor<?> constructor, Object[] args);
}
