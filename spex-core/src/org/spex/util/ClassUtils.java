package org.spex.util;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ClassUtils {

	public static final String ARRAY_SUFFIX = "[]";
	
	private static final String INTERNAL_ARRAY_PREFIX = "[";
	
	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";
	
	private static final String PACKAGE_SEPARATOR = ".";
	
	private static final String INNER_CLASS_SEPARATOR = "$";
	
	public static final String CGLIB_CLASS_SEPARATOR = "$$";
	
	public static final String POUND_SEPARATOR = "#";
	
	
	/** 原始包装类型 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);
	
	/** 原始类型-包装类型 */
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>(8);

	/** 原始类型-包装类型 */
	private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(16);

	/** 原始类型-包装类型 */
	private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(32);
	
	static {
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
		
		for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}
		
		Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(16);
		primitiveTypes.addAll(primitiveWrapperTypeMap.values());
		primitiveTypes.addAll(Arrays.asList(
				int[].class, float[].class, double[].class, boolean[].class, 
				byte[].class, char[].class, long[].class, short[].class));
		
		for (Class<?> clazz : primitiveTypes) {
			primitiveTypeNameMap.put(clazz.getName(), clazz);
		}
		
		registerCommonClasses(Integer[].class, Float[].class, Double[].class, Boolean[].class,
				Byte[].class, Character[].class, Long[].class, Short[].class);
		registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
				Object.class, Object[].class, Class.class, Class[].class);
		registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
				Error.class, StackTraceElement.class, StackTraceElement[].class);
	}
	
	private static void registerCommonClasses(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			commonClassCache.put(clazz.getName(), clazz);
		}
	}
	
	public static boolean isAssignableValue(Class<?> type, Object value) {
		if (type == null) throw new IllegalArgumentException("type为空");
		
		return value != null ? isAssignableFrom(type, value.getClass()) : !type.isPrimitive();
	}
	
	/**
	 * 左侧类是否是右侧类的父类或父接口，或者左侧类是否就是右侧类
	 * @param leftClass
	 * @param rightClass
	 * @return 是否
	 */
	public static boolean isAssignableFrom(Class<?> leftClass, Class<?> rightClass) {
		if (leftClass == null) throw new IllegalArgumentException("左侧Class为空");
		if (rightClass == null) throw new IllegalArgumentException("右侧Class为空");
		
		return leftClass.isAssignableFrom(rightClass) ||
				leftClass.equals(primitiveWrapperTypeMap.get(rightClass));
	}
	
	/**
	 * com.xx.xx.A -> A
	 * @param className
	 * @return A
	 */
	public static String getShortName(String className) {
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		String shortName = className.substring(lastDotIndex + 1, className.length());
		shortName = shortName.replaceAll(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}
	
	public static String getShortName(Class<?> clazz) {
		return getShortName(getQualifiedName(clazz));
	}
	
	public static String getShortNameAsProperty(Class<?> clazz) {
		String shortName = getShortName(clazz);
		int dotIndex = shortName.indexOf('.');
		shortName = dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName;
		return Introspector.decapitalize(shortName);
	}
	
	public static boolean matchesTypeName(Class<?> clazz, String typeName) {
		return (typeName != null &&
				(typeName.equals(clazz.getName()) || typeName.equals(clazz.getSimpleName()) ||
						(clazz.isArray() && typeName.equals(getQualifiedNameForArray(clazz)))));
	}
	
	public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException {
		if (name == null) {
			throw new IllegalArgumentException("'name'不能为空");
		}
		
		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			clazz = commonClassCache.get(name);
		}
		if (clazz != null) {
			return clazz;
		}
		
		// "java.util.String[]" 处理类似的数组
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elemClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?> elemClass = forName(elemClassName, classLoader);
			return Array.newInstance(elemClass, 0).getClass();
		}
		
		// "[Ljava.util.String;" 处理类似的数组
		if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
			String elemClassName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
			Class<?> elemClass = forName(elemClassName, classLoader);
			return Array.newInstance(elemClass, 0).getClass();
		}
		
		// "[[I 或 [[Ljava.util.String;" 处理类似的数组
		if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
			String elemClassName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?> elemClass = forName(elemClassName, classLoader);
			return Array.newInstance(elemClass, 0).getClass();
		}
		
		ClassLoader clToUse = classLoader;
		if (clToUse == null) {
			clToUse = getDefaultClassLoader();
		}
		
		try {
			return clToUse.loadClass(name);
		} catch (ClassNotFoundException e) {
			// 处理内部类
			int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
				
				try {
					return clToUse.loadClass(innerClassName);
				} catch (ClassNotFoundException e1) {
					// 
				}
			}
			throw e;
		}
	}
	
	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> clazz = null;
		// 由于基本类型的名字长度都不长，所以对长度加以限制
		if (name != null && name.length() <= 8) {
			clazz = primitiveTypeNameMap.get(name);
		}
		return clazz;
	}
	
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable e) {
			
		}
		
		if (cl == null) {
			cl = ClassUtils.class.getClassLoader();
		}
		return cl;
	}
	
	public static String getQualifiedName(Class<?> clazz) {
		if (clazz.isArray()) {
			return getQualifiedName(clazz);
		} else {
			return clazz.getName();
		}
	}
	
	/**
	 * String[][][][]，多维数组
	 * @param clazz
	 * @return
	 */
	private static String getQualifiedNameForArray(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		while(clazz.isArray()) {
			clazz = clazz.getComponentType();
			sb.append("[]");
		}
		sb.insert(0, clazz.getName());
		return sb.toString();
	}
	
	public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
		return getMethodIfAvailable(clazz, methodName, paramTypes) != null;
	}
	
	public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}
	
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		return primitiveWrapperTypeMap.containsKey(clazz);
	}
	
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}
	
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader cl) {
		if (clazz.isInterface()) {
			return new Class<?>[] {clazz};
		}
		
		List<Class<?>> interfaces = new ArrayList<Class<?>>();
		while (clazz != null) {
			Class<?>[] ifs = clazz.getInterfaces();
			for (Class<?> ifc : ifs) {
				if (!interfaces.contains(ifc) &&
						(cl == null || isVisable(ifc, cl))) {
					interfaces.add(ifc);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces.toArray(new Class<?>[interfaces.size()]);
	}
	
	/**
	 * 校验指定类是否在ClassLoader中可见
	 * @param clazz
	 * @param cl
	 * @return
	 */
	public static boolean isVisable(Class<?> clazz, ClassLoader cl) {
		if (cl == null) {
			return true;
		}
		
		try {
			Class<?> actualClass = cl.loadClass(clazz.getName());
			return clazz == actualClass;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static boolean isCacheSafe(Class<?> clazz, ClassLoader cl) {
		ClassLoader targetCl = clazz.getClassLoader();
		if (targetCl == null) {
			return false;
		}
		
		ClassLoader curCl = cl;
		if (cl == targetCl) {
			return true;
		}
		while (curCl != null) {
			curCl = curCl.getParent();
			if (curCl == targetCl) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			forName(className, classLoader);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static Class<?> getUserClass(Class<?> clazz) {
		return (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) ? clazz.getSuperclass() : clazz;
	}

	public static String getDescriptiveType(Object value) {
		if (value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if (Proxy.isProxyClass(clazz)) {
			StringBuilder result = new StringBuilder(clazz.getName());
			result.append(" implementing ");
			Class<?>[] ifcs = clazz.getInterfaces();
			for (int i = 0; i < ifcs.length; i++) {
				result.append(ifcs[i].getName());
				if (i < ifcs.length - 1) {
					result.append(", ");
				}
			}
			return result.toString();
		} else if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		} else {
			return clazz.getName();
		}
	}
}
