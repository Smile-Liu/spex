package org.spex.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.spex.core.MethodParameter;
import org.spex.util.ClassUtils;
import org.spex.util.ReflectionUtils;

public class BeanUtils {

	private static final Set<Class<?>> unknownEditorSet = Collections.synchronizedSet(new HashSet<Class<?>>());
	
	public static PropertyEditor findEditorByConvention(Class<?> targetType) {
		if (targetType == null || targetType.isArray() || unknownEditorSet.contains(targetType)) {
			return null;
		}
		
		ClassLoader cl = targetType.getClassLoader();
		if (cl == null) {
			try {
				cl = ClassLoader.getSystemClassLoader();
				if (cl == null) {
					return null;
				}
			} catch (Throwable e) {
				return null;
			}
		}
		
		String editorName = targetType.getName() + "Editor";
		try {
			Class<?> editorClass = cl.loadClass(editorName);
			if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
				unknownEditorSet.add(editorClass);
				return null;
			}
			
			return (PropertyEditor) instantiateClass(editorClass);
		} catch (ClassNotFoundException e) {
			unknownEditorSet.add(targetType);
			return null;
		}
	}
	
	public static <T> T instantiateClass(Class<T> clazz) {
		if (clazz.isInterface()) {
			throw new RuntimeException(clazz.getName() + " 指定类型是接口导致实例化失败");
		}
		try {
			return instantiateClass(clazz.getDeclaredConstructor());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(clazz.getName() + " 没有找到默认构造器", e);
		}
	}
	
	public static <T> T instantiateClass(Constructor<T> ctor, Object... args) {
		try {
			ReflectionUtils.makeAccessible(ctor);
			return ctor.newInstance(args);
			
		} catch (InstantiationException e) {
			
			throw new RuntimeException(ctor.getDeclaringClass().getName() + " 是抽象类？", e);
		} catch (IllegalArgumentException e) {

			throw new RuntimeException(ctor.getDeclaringClass().getName() + " 非法参数", e);
		} catch (IllegalAccessException e) {

			throw new RuntimeException(ctor.getDeclaringClass().getName() + " 构造器可以访问？", e);
		} catch (InvocationTargetException e) {

			throw new RuntimeException(ctor.getDeclaringClass().getName() + " 构造器抛出异常", e.getTargetException());
		}
	}
	
	public static boolean isSimpleProperty(Class<?> clazz) {
		return (isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType())));
	}
	
	public static boolean isSimpleValueType(Class<?> clazz) {
		return (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || 
				CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) ||
				Date.class.isAssignableFrom(clazz) || clazz.equals(URI.class) ||
				clazz.equals(URL.class) || clazz.equals(Locale.class) || clazz.equals(Class.class));
		
	}
	
	public static MethodParameter getWriterMethodParameter(PropertyDescriptor pd) {
		if (pd instanceof GenericTypeAwarePropertyDescriptor) {
			return new MethodParameter(((GenericTypeAwarePropertyDescriptor) pd).getWriterMethodParameter());
		} else {
			return new MethodParameter(pd.getWriteMethod(), 0);
		}
	}

	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			return findDeclaredMethod(clazz, methodName, paramTypes);
		}
	}

	public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getDeclaredMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
			}
			return null;
		}
	}
}
