package org.spex.beans.factory.support;

import java.lang.reflect.Method;

import org.spex.beans.factory.config.AbstractBeanDefinition;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;

public class RootBeanDefinition extends AbstractBeanDefinition {

	/** ��ʶ�Ƿ�������ʵ����֮ǰִ��BeanPostProcessor */
	volatile Boolean beforeInstantiationResolved;

	/** �����Ѿ�������Ĺ������򹤳����� */
	volatile Object resolvedConstructorOrFactoryMethod;
	
	/** �����Ѿ�������Ĺ��������߹��������Ĳ��� */
	volatile Object[] resolvedConstructorArguments;
	
	/** Ԥ�ȷ��롢��Ҫ����Ĳ��� */
	volatile Object[] preparedConstructorArguments;
	
	
	volatile boolean constructorArgumentsResolved = false;
	
	boolean isFactoryMethodUnique;
	
	private BeanDefinitionHolder decoratedDefinition;
	
	public RootBeanDefinition() {
		super();
	}
	
	public RootBeanDefinition(Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}
	
	public RootBeanDefinition(BeanDefinition original) {
		super(original);
		if (original instanceof RootBeanDefinition) {
			RootBeanDefinition originalRbd = (RootBeanDefinition) original;
			this.isFactoryMethodUnique = originalRbd.isFactoryMethodUnique;
		}
	}
	
	public void setUniqueFactoryMethod(String name) {
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}
	
	public boolean isFactoryMethod(Method candidate) {
		return candidate != null && candidate.getName().equals(getFactoryMethodName());
	}

	public BeanDefinitionHolder getDecoratedDefinition() {
		return decoratedDefinition;
	}

	public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}
	
}
