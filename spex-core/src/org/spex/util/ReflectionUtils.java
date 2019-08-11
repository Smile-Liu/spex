package org.spex.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ���乤����
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
	 * ��ȡ�����̳����ϵ����з���
	 * @param clazz Ŀ����
	 * @return ��������
	 */
	public static Method[] getAllDeclaredMethod(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("'clazz' ����Ϊ��");
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
		
		// �����ҵ������̳����ϵ����з���
		do {
			Method[] methods = targetClass.getDeclaredMethods();
			for (Method m : methods) {
				if (mf != null && !mf.matches(m)) {
					continue;
				}
				
				try {
					mc.doWith(m);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("���ܷ��ʷ��� " + m.getName() + " ��" + e);
				}
			}
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null);
	}
	
	/**
	 * ƥ��Ŀ�����Լ���̳�������ָ���������ͷ���������ͬ�ķ���
	 * @param clazz Ŀ����
	 * @param name ָ��������
	 * @param paramTypes ָ��������������
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
