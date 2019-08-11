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
		
		// ��Ϊ��֧��replaced-method��lookup-method�����Բ���Ҫʹ��CglibSubclassingInstantiationStrategy
		Constructor<?> ctor = (Constructor<?>) beanDefinition.resolvedConstructorOrFactoryMethod;
		if (ctor == null) {
			final Class<?> clazz = beanDefinition.getBeanClass();
			if (clazz.isInterface()) {
				throw new BeanCreationException(beanName, "ָ�������ǽӿ�");
			} else {
				try {
					ctor = clazz.getDeclaredConstructor((Class<?>[]) null);
					beanDefinition.resolvedConstructorOrFactoryMethod = ctor;
				} catch (Throwable e) {
					throw new BeanCreationException(beanName, "ָ����û��Ĭ�Ϲ�����", e);
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

			throw new BeanDefinitionStoreException("�������� " + factoryMethod.getName() + ",�Ƿ����� " + StringUtils.arrayToCommaDelimitedString(args), e);
		} catch (IllegalAccessException e) {

			throw new BeanDefinitionStoreException("�������� " + factoryMethod.getName() + "���ܷ���", e);
		} catch (InvocationTargetException e) {
 
			throw new BeanDefinitionStoreException("�������� " + factoryMethod.getName() + " �׳��쳣", e);
		}
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, 
			Constructor<?> constructor, Object[] args) {
		return null;
	}

}
