package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanDefinition;

/**
 * BeanDefinition的工具类
 * 包括注册、移除、获取、校验等功能
 * @author hp
 *
 */
public interface BeanDefinitionRegistry {

	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
	
	void removeBeanDefinition(String beanName);
	
	BeanDefinition getBeanDefinition(String beanName);
	
	boolean containsBeanDefinition(String beanName);
	
	int getBeanDefinitionCount();
	
	String[] getBeanDefinitionNames();
}
