package org.spex.core.io.support;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.spex.util.ObjectUtils;

/**
 * ���ϴ�������
 * @author hp
 *
 */
public class CollectionUtils {

	
	/**
	 * ������ֵ�еĸ������Զ��ŵ�map�ṹ��
	 * @param props ����
	 * @param map ����
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergeProperties2Map(Properties props, Map map) {
		if (props == null || map == null) {
			throw new IllegalArgumentException("���Ի򼯺ϲ���Ϊ��");
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
			throw new IllegalArgumentException("����Ϊ��");
		}
		Object[] arr = ObjectUtils.toObjectArray(array);
		for (Object elem : arr) {
			collection.add(elem);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergePropertiesIntoMap(Properties props, Map map) {
		if (map == null) {
			throw new IllegalArgumentException("Map����Ϊ��");
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
