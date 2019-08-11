package org.spex.core;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.spex.util.ClassUtils;

public class Conventions {

	private static final String PLURALIZE_SUFFIX = "List";
	private static final Set<Class<?>> ignoreInterfaces = new HashSet<Class<?>>();
	
	static {
		ignoreInterfaces.add(Serializable.class);
		ignoreInterfaces.add(Externalizable.class);
		ignoreInterfaces.add(Cloneable.class);
		ignoreInterfaces.add(Comparable.class);
	}
	
	public static String getVariableName(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		Class<?> valueClass;
		boolean pluralize = false;
		
		if (value.getClass().isArray()) {
			valueClass = value.getClass().getComponentType();
			pluralize = true;
		} else if (value instanceof Collection) {
			Collection<?> col = (Collection<?>) value;
			if (col.isEmpty()) {
				throw new IllegalArgumentException("空集合不能生成变量名称");
			}
			Object valueToCheck = peekAhead(col);
			valueClass = getClassForValue(valueToCheck);
			pluralize = true;
		} else {
			valueClass = getClassForValue(value);
		}
		String name = ClassUtils.getShortNameAsProperty(valueClass);
		return pluralize ? pluralize(name) : name;
	}
	
	private static String pluralize(String name) {
		return name + PLURALIZE_SUFFIX;
	}
	
	private static Class<?> getClassForValue(Object value) {
		Class<?> valueClass = value.getClass();
		if (Proxy.isProxyClass(valueClass)) {
			Class<?>[] ifcs = valueClass.getInterfaces();
			for (Class<?> ifc : ifcs) {
				if (!ignoreInterfaces.contains(ifc)) {
					return ifc;
				}
			}
		} else if (valueClass.getName().indexOf('$') != -1 && valueClass.getDeclaringClass() == null) {
			valueClass = valueClass.getSuperclass();
		}
		return valueClass;
	}
	
	private static Object peekAhead(Collection<?> collection) {
		Iterator<?> it = collection.iterator();
		if (!it.hasNext()) {
			throw new IllegalStateException("非空集合没有元素，不能读取");
		}
		Object value = it.next();
		if (value == null) {
			throw new IllegalStateException("非空集合只包含空元素，不能读取");
		}
		return value;
	}
}
