package org.spex.beans.factory.config;

import java.lang.reflect.Constructor;

public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName);
	
	Object getEarlyBeanReference(Object bean, String beanName);
}
