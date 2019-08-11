package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanFactoryPostProcessor;

public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {
	
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry);

}
