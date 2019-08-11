package org.spex.beans.factory.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spex.beans.BeansException;
import org.spex.beans.TypeConverter;
import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.FactoryBean;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.RuntimeBeanReference;
import org.spex.beans.factory.config.TypedStringValue;
import org.spex.util.ClassUtils;

public class BeanDefinitionValueResolver {

	private final AbstractBeanFactory beanFactory;
	
	private final String beanName;
	
	private final BeanDefinition beanDefinition;
	
	private final TypeConverter typeConverter;
	
	public BeanDefinitionValueResolver(AbstractBeanFactory beanFactory, String beanName, 
			BeanDefinition beanDefinition, TypeConverter typeConverter) {
		
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.typeConverter = typeConverter;
	}
	
	
	public Object resolveValueIfNecessary(Object argName, Object value) {
		// ref����
		if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(argName, ref);
		}
		
		// �ӱ�ǩbean
		if (value instanceof BeanDefinitionHolder) {
			BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
			return resolveInnerBean(argName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
		}
		
		if (value instanceof BeanDefinition) {
			BeanDefinition bd = (BeanDefinition) value;
			return resolveInnerBean(argName, "(inner bean)", bd);
		}
		
		// �ӱ�ǩlist
		if (value instanceof List) {
			return resolveList(argName, (List<?>) value);
		}

		// �ӱ�ǩset
		if (value instanceof Set) {
			return resolveSet(argName, (Set<?>) value);
		}

		// �ӱ�ǩmap
		if (value instanceof Map) {
			return resolveMap(argName, (Map<?, ?>) value);
		}
		
		// ������������������ TypedStringValue
		if (value instanceof TypedStringValue) {
			TypedStringValue typedStringValue = (TypedStringValue) value;
			Object valueObj = evaluate(typedStringValue);
			
			try {
				Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
				if (resolvedTargetType != null) {
					return this.typeConverter.convertIfNecessary(valueObj, resolvedTargetType);
				} else {
					return valueObj;
				}
			} catch (Throwable ex) {
				throw new BeanCreationException(this.beanName, "ת��TypedStringValueֵʧ�ܣ�" + argName, ex);
			}
		}
		return value;
	}
	
	protected Object evaluate(TypedStringValue value) {
		Object result = this.beanFactory.evaluateBeanDefinitionString(value.getValue(), this.beanDefinition);
		if (result != value.getValue()) {
//			value.setD
		}
		return result;
	}
	
	protected Class<?> resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
	}
	
	private Object resolveReference(Object argName, RuntimeBeanReference ref) {
		try {
			String refBeanName = ref.getBeanName();
			Object bean = this.beanFactory.getBean(refBeanName);
			this.beanFactory.registerDependentBean(refBeanName, this.beanName);
			return bean;
		} catch (BeansException e) {
			throw new BeanCreationException(this.beanName, "���ܽ���������" + argName + "������Bean��" + ref.getBeanName(), e);
		}
	}
	
	private Object resolveList(Object argName, List<?> list) {
		List<Object> result = new ArrayList<Object>(list.size());
		for (int i = 0; i < list.size(); i++) {
			result.add(resolveValueIfNecessary(argName + "[" + i + "]", list.get(i)));
		}
		return result;
	}
	
	private Object resolveSet(Object argName, Set<?> set) {
		Set<Object> result = new HashSet<Object>(set.size());
		int i = 0;
		for (Object s : set) {
			result.add(resolveValueIfNecessary(argName + "[" + i + "]", s));
			i++;
		}
		return result;
	}
	
	private Object resolveMap(Object argName, Map<?, ?> map) {
		Map<Object, Object> result = new LinkedHashMap<Object, Object>(map.size());
		for (Entry<?, ?> entry : map.entrySet()) {
			result.put(entry.getKey(), resolveValueIfNecessary(argName + "[" + entry.getKey() + "]", entry.getValue()));
		}
		return result;
	}
	private Object resolveInnerBean(Object argName, String innerBeanName, BeanDefinition bd) {
		RootBeanDefinition mbd = null;
		try {
			mbd = this.beanFactory.getMergedLocalBeanDefinition(innerBeanName);
			
			String actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String depend : dependsOn) {
					this.beanFactory.getBean(depend);
					this.beanFactory.registerDependentBean(depend, actualInnerBeanName);
				}
			}
			
			Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mbd, null);
			this.beanFactory.registerContainedBean(actualInnerBeanName, this.beanName);
			if (innerBean instanceof FactoryBean) {
				return this.beanFactory.getObjectFromFactoryBean((FactoryBean<?>) innerBean, actualInnerBeanName);
			} else {
				return innerBean;
			}
		} catch (BeansException e) {
			throw new BeanCreationException(beanName, "�������� " + argName + " ָ������Ԫ��bean " + innerBeanName + " ʧ��", e);
		}
	}
	
	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		
		int counter = 0;
		while (this.beanFactory.isBeanNameInUsed(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = innerBeanName + ClassUtils.POUND_SEPARATOR + counter;
		}
		return actualInnerBeanName;
	}
}
