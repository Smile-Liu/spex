package org.spex.util;

import java.lang.reflect.Array;

public class ObjectUtils {

	
	public static String identityToString(Object value) {
		if (value == null) {
			return null;
		}
		return value.getClass().getName() + "@" + identityHexString(value);
	}
	
	public static String identityHexString(Object value) {
		return Integer.toHexString(System.identityHashCode(value));
	}
	
	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}
	
	public static Object[] toObjectArray(Object obj) {
		if (obj instanceof Object[]) {
			return (Object[]) obj;
		}
		if (obj == null) {
			return new Object[0];
		}
		if (!obj.getClass().isArray()) {
			throw new IllegalStateException(obj + "不是一个数组");
		}
		int length = Array.getLength(obj);
		if (length == 0) {
			return new Object[0];
		}
		Class<?> wrapperType = Array.get(obj, 0).getClass();
		Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			newArray[i] = Array.get(obj, i);
		}
		return newArray;
	}
}
