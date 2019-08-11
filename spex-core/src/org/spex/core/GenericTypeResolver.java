package org.spex.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class GenericTypeResolver {

	private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> typevariableCache = 
		Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
	
	
	public static Type getTargetType(MethodParameter methodParam) {
		if (methodParam == null) throw new IllegalArgumentException("MethodParameter不能为空");
		
		if (methodParam.getConstructor() != null) {
			return methodParam.getConstructor().getGenericParameterTypes()[methodParam.getParameterIndex()];
		}
		if (methodParam.getParameterIndex() >= 0) {
			return methodParam.getMethod().getGenericParameterTypes()[methodParam.getParameterIndex()];
		}
		return methodParam.getMethod().getGenericReturnType();
	}
	
	public static Class<?> resolveParameterType(MethodParameter methodParam, Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("必须指定class");
		}
		
		Type genericType = getTargetType(methodParam);
		Map<TypeVariable<?>, Type> typeVariableMap = getTypeVariableMap(clazz);
		Type rawType = getRawType(genericType, typeVariableMap);
		Class<?> result = (rawType instanceof Class) ? (Class<?>) rawType : methodParam.getParameterType();
		methodParam.setParameterType(result);
		methodParam.setTypeVariableMap(typeVariableMap);
		
		return result;
	}
	
	static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> clazz) {
		Reference<Map<TypeVariable<?>, Type>> ref = typevariableCache.get(clazz);
		Map<TypeVariable<?>, Type> typeVariableMap = ref == null ? null : ref.get();
		
		if (typeVariableMap == null) {
			typeVariableMap = new HashMap<TypeVariable<?>, Type>();
			
			// interface
			extractTypeVariableFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);
			
			// super class
			Type genericType = clazz.getGenericSuperclass();
			Class<?> type = clazz.getSuperclass();
			
			while (type != null && !Object.class.equals(type)) {
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeVariableFromParameterizedType(pt, typeVariableMap);
				}
				extractTypeVariableFromGenericInterfaces(type.getGenericInterfaces(), typeVariableMap);
				
				genericType = type.getGenericSuperclass();
				type = type.getSuperclass();
			}
			
			// enclosing class（内部类）
			type = clazz;
			while (type.isMemberClass()) {
				genericType = type.getGenericSuperclass();
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeVariableFromParameterizedType(pt, typeVariableMap);
				}
				type = type.getEnclosingClass();
			}
			
			typevariableCache.put(clazz, new WeakReference<Map<TypeVariable<?>, Type>>(typeVariableMap));
		}
		return typeVariableMap;
	}
	
	static Class<?> resolveType(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap) {
		Type rawType = getRawType(genericType, typeVariableMap);
		return rawType instanceof Class ? (Class<?>) rawType : Object.class;
	}
	
	static Type getRawType(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap) {
		Type resolvedType = genericType;
		if (genericType instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) genericType;
			resolvedType = typeVariableMap.get(tv);
			if (resolvedType == null) {
				resolvedType = extractBoundForTypeVariable(tv);
			}
		}
		if (resolvedType instanceof ParameterizedType) {
			return ((ParameterizedType) resolvedType).getRawType();
		}
		return resolvedType;
	}
	
	static Type extractBoundForTypeVariable(TypeVariable<?> typeVariable) {
		Type[] bounds = typeVariable.getBounds();
		if (bounds == null) {
			return Object.class;
		}
		Type bound = bounds[0];
		if (bound instanceof TypeVariable) {
			bound = extractBoundForTypeVariable((TypeVariable<?>) bound);
		}
		return bound;
	}
	
	public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericIfc) {
		Class<?>[] types = resolveTypeArguments(clazz, genericIfc);
		if (types == null) {
			return null;
		}
		if (types.length != 1) {
			throw new IllegalStateException("发现" + types.length + "个类型值 [" + genericIfc.getName() + "]，但只要1个。");
		}
		return types[0];
	}
	
	public static Class<?>[] resolveTypeArguments(Class<?> clazz, Class<?> genericIfc) {
		return doResolveTypeArguments(clazz, clazz, genericIfc);
	}
	
	private static Class<?>[] doResolveTypeArguments(Class<?> ownClass, Class<?> classToIntrospect, Class<?> genericIfc) {
		while (classToIntrospect != null) {
			Type[] ifcs = classToIntrospect.getGenericInterfaces();
			
			for (Type ifc : ifcs) {
				if (ifc instanceof ParameterizedType) {
					ParameterizedType paramIfc = (ParameterizedType) ifc;
					Type rawType = paramIfc.getRawType();
					if (genericIfc.equals(rawType)) {
						Type[] typeArgs = paramIfc.getActualTypeArguments();
						Class<?>[] result = new Class<?>[typeArgs.length];
						for (int i = 0; i < typeArgs.length; i++) {
							Type arg = typeArgs[i];
							result[i] = extractClass(ownClass, arg);
						}
						return result;
					}
					else if (genericIfc.isAssignableFrom((Class<?>) rawType)) {
						return doResolveTypeArguments(ownClass, (Class<?>) rawType, genericIfc);
					}
				}
				else if (genericIfc.isAssignableFrom((Class<?>) ifc)) {
					return doResolveTypeArguments(ownClass, (Class<?>) ifc, genericIfc);
				}
			}
			classToIntrospect = classToIntrospect.getSuperclass();
		}
		return null;
	}
	
	private static Class<?> extractClass(Class<?> ownClass, Type arg) {
		if (arg instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) arg;
			arg = getTypeVariableMap(ownClass).get(tv);
			
			if (arg == null) {
				arg = extractBoundForTypeVariable(tv);
			} else {
				arg = extractClass(ownClass, arg);
			}
		} else if (arg instanceof GenericArrayType) {
			GenericArrayType gat = (GenericArrayType) arg;
			Type gt = gat.getGenericComponentType();
			Class<?> componentClass = extractClass(ownClass, gt);
			arg = Array.newInstance(componentClass, 0).getClass();
		}
		
		return arg instanceof Class ? (Class<?>) arg : Object.class;
	}
	
	private static void extractTypeVariableFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable<?>, Type> typeVariableMap) {
		for (Type genericInterface : genericInterfaces) {
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericInterface;
				populateTypeVariableFromParameterizedType(pt, typeVariableMap);
				
				if (pt.getRawType() instanceof Class) {
					extractTypeVariableFromGenericInterfaces(((Class<?>) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
				}
			} else if (genericInterface instanceof Class) {
				extractTypeVariableFromGenericInterfaces(((Class<?>) genericInterface).getGenericInterfaces(), typeVariableMap);
			}
		}
	}
	
	private static void populateTypeVariableFromParameterizedType(ParameterizedType type, Map<TypeVariable<?>, Type> typeVariableMap) {
		if (type.getRawType() instanceof Class) {
			Type[] actualTypeArguments = type.getActualTypeArguments();
			TypeVariable<?>[] typeVariables = ((Class<?>)type.getRawType()).getTypeParameters();
			
			for (int i = 0; i < actualTypeArguments.length; i++) {
				Type actualTypeArgument = actualTypeArguments[i];
				TypeVariable<?> variable = typeVariables[i];
				
				if (actualTypeArgument instanceof Class || actualTypeArgument instanceof GenericArrayType ||
						actualTypeArgument instanceof ParameterizedType) {
					typeVariableMap.put(variable, actualTypeArgument);
					
				} else if (actualTypeArgument instanceof TypeVariable<?>) {
					TypeVariable<?> typeVariable = (TypeVariable<?>) actualTypeArgument;
					Type resolvedType = typeVariableMap.get(typeVariable);
					if (resolvedType == null) {
						resolvedType = extractBoundForTypeVariable(typeVariable);
					}
					typeVariableMap.put(variable, resolvedType);
				}
				
			}
		}
	}
}
