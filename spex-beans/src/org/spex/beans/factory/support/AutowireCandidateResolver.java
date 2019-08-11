package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.DependencyDescriptor;

/**
 * 接口，判断指定的BeanDefinition是否是指定依赖的自动候选者
 * @author hp
 *
 */
public interface AutowireCandidateResolver {

	
	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);
	
	
	Object getSuggestedValue(DependencyDescriptor descriptor);
}
