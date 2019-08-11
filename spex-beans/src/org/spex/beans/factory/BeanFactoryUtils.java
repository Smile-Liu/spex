package org.spex.beans.factory;

import java.util.LinkedHashMap;
import java.util.Map;

import org.spex.beans.BeansException;
import org.spex.util.StringUtils;

public class BeanFactoryUtils {

	
	/**
	 * 判断是否是FactoryBean
	 * @param name bean name
	 * @return 是否
	 */
	public static boolean isFactoryDereference(String name) {
		return name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX);
	}
	
	
	/**
	 * 返回真正的Bean Name，去掉FactoryBean的前缀$
	 * @param name 用户指定的bean name
	 * @return 真正的bean name
	 */
	public static String transformedBeanName(String name) {
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("'name'不能为空");
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
