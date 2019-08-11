package org.spex.aop.support;

import org.spex.aop.SpringProxy;
import org.spex.aop.TargetClassAware;
import org.spex.util.ClassUtils;

public class AopUtils {

	
	public static Class<?> getTargetClass(Object candidate) {
		Class<?> result = null;
		
		if (candidate instanceof TargetClassAware) {
			result = ((TargetClassAware) candidate).getTargetClass();
		}
		
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}
	
	public static boolean isCglibProxy(Object obj) {
		return obj instanceof SpringProxy && isCglibProxyClass(obj.getClass());
	}
	
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return clazz != null && isCglibProxyClassName(clazz.getName());
	}
	
	public static boolean isCglibProxyClassName(String className) {
		return className != null && className.contains(ClassUtils.CGLIB_CLASS_SEPARATOR);
	}
}
