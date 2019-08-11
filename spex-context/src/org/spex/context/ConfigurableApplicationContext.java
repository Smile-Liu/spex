package org.spex.context;

import org.spex.beans.BeansException;
import org.spex.beans.factory.config.BeanFactoryPostProcessor;

public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle {

	String CONFIG_LOCATION_DELIMITTERS = ",; \t\n";
	
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";
	
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";
	
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";
	
	
	void setId(String id);
	
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);
	
	void addApplicationListener(ApplicationListener<?> listener);
	
	void refresh() throws BeansException, IllegalStateException;
	
	void close();
	
	boolean isActive();
	
}
