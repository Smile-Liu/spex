package org.spex.beans.factory.support;

import java.lang.reflect.Method;

import org.spex.beans.factory.config.AbstractBeanDefinition;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;

public class RootBeanDefinition extends AbstractBeanDefinition {

	/** 标识是否允许在实例化之前执行BeanPostProcessor */
	volatile Boolean beforeInstantiationResolved;

	/** 缓存已经处理过的构造器或工厂方法 */
	volatile Object resolvedConstructorOrFactoryMethod;
	
	/** 缓存已经处理过的构造器或者工厂方法的参数 */
	volatile Object[] resolvedConstructorArguments;
	
	/** 预先放入、需要处理的参数 */
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
