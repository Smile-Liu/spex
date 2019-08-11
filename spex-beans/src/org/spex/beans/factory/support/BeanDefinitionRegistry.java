package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanDefinition;

/**
 * BeanDefinition�Ĺ�����
 * ����ע�ᡢ�Ƴ�����ȡ��У��ȹ���
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
