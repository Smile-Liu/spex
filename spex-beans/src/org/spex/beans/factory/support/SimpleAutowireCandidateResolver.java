package org.spex.beans.factory.support;

import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.DependencyDescriptor;

public class SimpleAutowireCandidateResolver implements AutowireCandidateResolver {

	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		return bdHolder.getBeanDefinition().isAutowireCandidate();
	}

	@Override
	public Object getSuggestedValue(DependencyDescriptor descriptor) {
		return null;
	}

}
