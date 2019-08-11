package org.spex.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 * @author hp
 */
public class ReflectionUtils {

	public static void makeAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
				&& !method.isAccessible()) {
			method.setAccessible(true);
		}
	}
	
	public static void makeAccessible(Constructor<?> ctor) {
		if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))
				&& !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}
	}
	
	/**
	 * 获取整个继承链上的所有方法
	 * @param clazz 目标类
	 * @return 方法数组
	 */
	public static Method[] getAllDeclaredMethod(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("'clazz' 参数为空");
		}
		
		final List<Method> methods = new ArrayList<Method>();
		doWithMethods(clazz, new MethodCallback() {

			@Override
			public void doWith(Method method) {
				methods.add(method);
			}
			
		});
		
		return methods.toArray(new Method[methods.size()]);
	}
	
	public static void doWithMethods(Class<?> leafClass, MethodCallback mc) {
		doWithMethods(leafClass, mc, null);
	}

	public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) throws IllegalArgumentException {
		Class<?> targetClass = clazz;
		
		// 依次找到整个继承链上的所有方法
		do {
			Method[] methods = targetClass.getDeclaredMethods();
			for (Method m : methods) {
				if (mf != null && !mf.matches(m)) {
					continue;
				}
				
				try {
					mc.doWith(m);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("不能访问方法 " + m.getName() + " ：" + e);
				}
			}
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null);
	}
	
	/**
	 * 匹配目标类以及其继承链中与指定方法名和方法参数相同的方法
	 * @param clazz 目标类
	 * @param name 指定方法名
	 * @param paramTypes 指定方法参数类型
	 * @return Method
	 */
	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals(name) &&
						(paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	
	public interface MethodCallback {
		
		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
	}
	
	public interface MethodFilter {
		
		boolean matches(Method method);
	}
}
