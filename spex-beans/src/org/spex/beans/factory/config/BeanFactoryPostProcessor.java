package org.spex.beans.factory.config;

import org.spex.beans.factory.ListableBeanFactory;

/**
 * BeanFactory的后置处理器
 * @author hp
 *
 */
public interface BeanFactoryPostProcessor {

	
	void postProcessBeanFactory(ListableBeanFactory beanFactory);
}
