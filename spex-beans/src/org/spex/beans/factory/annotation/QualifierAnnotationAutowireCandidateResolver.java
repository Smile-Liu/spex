package org.spex.beans.factory.annotation;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.BeanFactoryAware;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.DependencyDescriptor;
import org.spex.beans.factory.support.AutowireCandidateResolver;

public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryAware {

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder,
			DependencyDescriptor descriptor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSuggestedValue(DependencyDescriptor descriptor) {
		// TODO Auto-generated method stub
		return null;
	}

}
