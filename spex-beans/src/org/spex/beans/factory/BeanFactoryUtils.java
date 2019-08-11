package org.spex.beans.factory;

import java.util.LinkedHashMap;
import java.util.Map;

import org.spex.beans.BeansException;
import org.spex.util.StringUtils;

public class BeanFactoryUtils {

	
	/**
	 * �ж��Ƿ���FactoryBean
	 * @param name bean name
	 * @return �Ƿ�
	 */
	public static boolean isFactoryDereference(String name) {
		return name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX);
	}
	
	
	/**
	 * ����������Bean Name��ȥ��FactoryBean��ǰ׺$
	 * @param name �û�ָ����bean name
	 * @return ������bean name
	 */
	public static String transformedBeanName(String name) {
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("'name'����Ϊ��");
		}
		
		String beanName = name;
		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanName;
	}
	
	
	public static String[] beanNameForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type, 
			boolean includeNonSingletons, boolean allowEagerInit) {
		
		String[] results = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		return results;
	}


	public static <T> Map<String, T> beansForTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type,
			boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {

		Map<String, T> results = new LinkedHashMap<String, T>();
		results.putAll(lbf.getBeansByType(type, includeNonSingletons, allowEagerInit));
		return results;
	}
}
