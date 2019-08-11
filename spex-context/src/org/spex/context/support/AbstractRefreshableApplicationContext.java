package org.spex.context.support;

import java.io.IOException;

import org.spex.beans.BeansException;
import org.spex.beans.factory.ListableBeanFactory;
import org.spex.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.spex.beans.factory.support.DefaultListableBeanFactory;
import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationContextException;
import org.spex.core.LocalVariableTableParameterNameDiscoverer;

public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	private Boolean allowBeanDefinitionOverriding;
	
	private Boolean allowCircularReferences;
	
	private DefaultListableBeanFactory beanFactory;
	
	private final Object beanFactoryMonitor = new Object();
	
	public AbstractRefreshableApplicationContext() {
		
	}
	
	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}
	
	public void setAllowBeanDefinitionOverriding(
			boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	@Override
	public ListableBeanFactory getBeanFactory() throws IllegalStateException {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory没有实例化或已关闭");
			}
			return this.beanFactory;
		}
	}

	@Override
	public void refreshBeanFactory() throws BeansException, IllegalStateException {
		if (hasBeanFactory()) {
			destoryBeans();
			closeBeanFactory();
		}
		
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			customizeBeanFactory(beanFactory);
			loadBeanDefinitions(beanFactory);
			
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		} catch (IOException e) {
			throw new ApplicationContextException("为" + getDisplayName() + "解析bean definition源失败", e);
		}
	}

	@Override
	protected final void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			this.beanFactory = null;
		}
	}
	
	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return this.beanFactory != null;
		}
	}
	
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory();
	}
	
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		beanFactory.setParameterNameDiscoverer(new LocalVariableTableParameterNameDiscoverer());
		beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
	}
	
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException;
}
