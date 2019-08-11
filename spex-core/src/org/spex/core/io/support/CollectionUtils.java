package org.spex.core.io.support;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.spex.util.ObjectUtils;

/**
 * 集合处理工具类
 * @author hp
 *
 */
public class CollectionUtils {

	
	/**
	 * 把属性值中的各个属性都放到map结构中
	 * @param props 属性
	 * @param map 集合
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergeProperties2Map(Properties props, Map map) {
		if (props == null || map == null) {
			throw new IllegalArgumentException("属性或集合不能为空");
		}
		
		for (Enumeration<String> en = (Enumeration<String>) props.propertyNames(); en.hasMoreElements();) {
			String key = en.nextElement();
			Object value = props.getProperty(key);
			if (value == null) {
				value = props.get(key);
			}
			map.put(key, value);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergeArrayIntoCollection(Object array, Collection collection) {
		if (collection == null) {
			throw new IllegalArgumentException("集合为空");
		}
		Object[] arr = ObjectUtils.toObjectArray(array);
		for (Object elem : arr) {
			collection.add(elem);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergePropertiesIntoMap(Properties props, Map map) {
		if (map == null) {
			throw new IllegalArgumentException("Map不能为空");
		}
		if (props != null) {
			for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				Object value = props.getProperty(key);
				if (value == null) {
					value = props.get(key);
				}
				map.put(key, value);
			}
		}
	}
	
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
}
