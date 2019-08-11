package org.spex.context.support;

import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.context.ApplicationContextAware;
import org.spex.context.ApplicationEventPublisherAware;
import org.spex.context.ConfigurableApplicationContext;

public class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private final ConfigurableApplicationContext applicationContext;
	
	public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public Object postProcessBeforeInitialization(final Object bean, String beanName) {
		invokeAwareInterfaces(bean);
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

	
	private void invokeAwareInterfaces(Object bean) {
		if (bean instanceof ApplicationEventPublisherAware) {
			((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
		}
		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}
	}
}
