package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.DependencyDescriptor;

/**
 * �ӿڣ��ж�ָ����BeanDefinition�Ƿ���ָ���������Զ���ѡ��
 * @author hp
 *
 */
public interface AutowireCandidateResolver {

	
	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);
	
	
	Object getSuggestedValue(DependencyDescriptor descriptor);
}
