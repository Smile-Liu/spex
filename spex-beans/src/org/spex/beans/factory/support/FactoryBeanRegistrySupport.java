package org.spex.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.BeanCurrentlyInCreationException;
import org.spex.beans.factory.FactoryBean;
import org.spex.beans.factory.FactoryBeanNotInitializedException;

public class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
	
	protected Class<?> getTypeForFactoryBean(FactoryBean<?> factoryBean) {
		return factoryBean.getObjectType();
	}
	
	public Object getCachedObjectForFactoryBean(String beanName) {
		Object obj = this.factoryBeanObjectCache.get(beanName);
		return obj;
	}
	
	public Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName) {
		// 不是单例，不计入缓存
		if (factory.isSingleton()) {
			synchronized (this.factoryBeanObjectCache) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if (object == null) {
					object = doGetObjectFromFactoryBean(factory, beanName);
					this.factoryBeanObjectCache.put(beanName, object);
				}
				return object;
			}
		} else {
			return doGetObjectFromFactoryBean(factory, beanName);
		}
	}
	
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanObjectCache.remove(beanName);
	}
	
	
	private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) {
		try {
			return factory.getObject();
		} catch (FactoryBeanNotInitializedException e) {
			throw new BeanCurrentlyInCreationException(beanName, e);
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "通过FactoryBean生成Bean时异常", e);
		}
	}
}
