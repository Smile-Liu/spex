package org.spex.beans.factory.config;

import org.spex.beans.factory.ListableBeanFactory;

/**
 * BeanFactory�ĺ��ô�����
 * @author hp
 *
 */
public interface BeanFactoryPostProcessor {

	
	void postProcessBeanFactory(ListableBeanFactory beanFactory);
}
