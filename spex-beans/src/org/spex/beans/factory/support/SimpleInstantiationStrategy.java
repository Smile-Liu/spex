package org.spex.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spex.beans.BeanUtils;
import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.BeanDefinitionStoreException;
import org.spex.beans.factory.BeanFactory;
import org.spex.util.ReflectionUtils;
import org.spex.util.StringUtils;

public class SimpleInstantiationStrategy implements InstantiationStrategy{

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		
		// 因为不支持replaced-method和lookup-method，所以不需要使用CglibSubclassingInstantiationStrategy
		Constructor<?> ctor = (Constructor<?>) beanDefinition.resolvedConstructorOrFactoryMethod;
		if (ctor == null) {
			final Class<?> clazz = beanDefinition.getBeanClass();
			if (clazz.isInterface()) {
				throw new BeanCreationException(beanName, "指定类型是接口");
			} else {
				try {
					ctor = clazz.getDeclaredConstructor((Class<?>[]) null);
					beanDefinition.resolvedConstructorOrFactoryMethod = ctor;
				} catch (Throwable e) {
					throw new BeanCreationException(beanName, "指定类没有默认构造器", e);
				}
			}
		}
		return BeanUtils.instantiateClass(ctor);
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, 
			Object factoryBean, Method factoryMethod, Object[] args) {
		ReflectionUtils.makeAccessible(factoryMethod);
		try {
			return factoryMethod.invoke(factoryBean, args);
		} catch (IllegalArgumentException e) {

			throw new BeanDefinitionStoreException("工厂方法 " + factoryMethod.getName() + ",非法参数 " + StringUtils.arrayToCommaDelimitedString(args), e);
		} catch (IllegalAccessException e) {

			throw new BeanDefinitionStoreException("工厂方法 " + factoryMethod.getName() + "不能访问", e);
		} catch (InvocationTargetException e) {
 
			throw new BeanDefinitionStoreException("工厂方法 " + factoryMethod.getName() + " 抛出异常", e);
		}
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, 
			Constructor<?> constructor, Object[] args) {
		return null;
	}

}
