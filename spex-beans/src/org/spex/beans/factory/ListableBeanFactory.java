package org.spex.beans.factory;

import java.util.Map;

import org.spex.beans.BeansException;
import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.beans.factory.config.SingletonBeanRegistry;
import org.spex.core.convert.ConversionService;

public interface ListableBeanFactory extends BeanFactory, SingletonBeanRegistry {

	String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);
	
	void setBeanClassLoader(ClassLoader classLoader);
	
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
	
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);
	
	<T> Map<String, T> getBeansByType(Class<T> type);
	
	<T> Map<String, T> getBeansByType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit);
	
	void setConversionService(ConversionService conversionService);
	
	ConversionService getConversionService();
	
	void freezeConfiguration();
	
	void preInstantiateSingletons();
	
	boolean isFactoryBean(String beanName);
	
	String[] getDependenciesForBean(String beanName);
	
	<T> T createBean(Class<T> beanClass) throws BeansException;
	
	Object initializeBean(Object existingBean, String beanName) throws BeansException;
}
