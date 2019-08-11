package org.spex.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.spex.util.ClassUtils;
import org.spex.util.StringUtils;

public class CachedIntrospectionResults {

	private BeanInfo beanInfo;
	
	/** ��Ӧbeaninfo�е��������ԵĻ��� */
	private Map<String, PropertyDescriptor> propertyDescriptorCache;
	
	static final Set<ClassLoader> acceptedClassLoaders = Collections.synchronizedSet(new HashSet<ClassLoader>());
	static final Map<Class<?>, Object> classCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, Object>());
	
	private CachedIntrospectionResults(Class<?> beanClass) {
		
		try {
			this.beanInfo = Introspector.getBeanInfo(beanClass);
			
			// ��Introspector Cache��ɾ��Class���Ա������������
			Class<?> classToFlush = beanClass;
			do {
				Introspector.flushFromCaches(classToFlush);
				classToFlush = classToFlush.getSuperclass();
			} while (classToFlush != null);
			
			this.propertyDescriptorCache = new HashMap<String, PropertyDescriptor>();
			
			// ����bean�е����ԣ����뻺����
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				pd = new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(), 
						pd.getWriteMethod(), pd.getPropertyEditorClass());
				this.propertyDescriptorCache.put(pd.getName(), pd);
			}
			
		} catch (IntrospectionException e) {
			throw new BeansException("��ȡ��" + beanClass.getName() + "BeanInfoʧ��", e);
		}
	}

	public BeanInfo getBeanInfo() {
		return beanInfo;
	}
	
	
	static CachedIntrospectionResults forClass(Class<?> beanClass) {
		CachedIntrospectionResults result;
		
		Object value = classCache.get(beanClass);
		if (value instanceof Reference) {
			Reference<?> ref = (Reference<?>) value;
			result = (CachedIntrospectionResults) ref.get();
		} else {
			result = (CachedIntrospectionResults) value;
		}
		
		if (result == null) {
			result = new CachedIntrospectionResults(beanClass);
			
			if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) ||
					isClassLoaderAccepted(beanClass.getClassLoader()) ||
					!ClassUtils.isPresent(beanClass.getName() + "BeanInfo", beanClass.getClassLoader())) {
				classCache.put(beanClass, result);
			} else {
				classCache.put(beanClass, new WeakReference<CachedIntrospectionResults>(result));
			}
		}
		return result;
	}
	
	private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
		ClassLoader[] acceptedLoaderArr = acceptedClassLoaders.toArray(new ClassLoader[acceptedClassLoaders.size()]);
		for (ClassLoader registedCl : acceptedLoaderArr) {
			if (isUnderneathClassLoader(classLoader, registedCl)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isUnderneathClassLoader(ClassLoader candidate, ClassLoader parent) {
		if (candidate == null) {
			return false;
		}
		 if (candidate == parent) {
			 return true;
		 }
		 ClassLoader classLoaderToCheck = candidate;
		 while (classLoaderToCheck != null) {
			 classLoaderToCheck = classLoaderToCheck.getParent();
			 if (classLoaderToCheck == parent) {
				 return true;
			 }
		 }
		 return false;
	}
	
	PropertyDescriptor[] getPropertyDescriptors() {
		Collection<PropertyDescriptor> descriptorCollection = this.propertyDescriptorCache.values();
		return descriptorCollection.toArray(new PropertyDescriptor[descriptorCollection.size()]);
	}
	
	PropertyDescriptor getPropertyDescriptor(String name) {
		PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
		if (pd == null && StringUtils.hasLength(name)) {
			// ���ȡ���������԰�name������ĸ��ɴ�д��Сд����ȡ
			pd = this.propertyDescriptorCache.get(name.substring(0, 1).toLowerCase() + name.substring(1));
			if (pd == null) {
				pd = this.propertyDescriptorCache.get(name.substring(0, 1).toUpperCase() + name.substring(1));
			}
		}
		return pd;
	}
}
