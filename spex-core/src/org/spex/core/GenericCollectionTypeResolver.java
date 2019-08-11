package org.spex.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

public class GenericCollectionTypeResolver {

	
	public static Class<?> getCollectionParameterType(MethodParameter methodParameter) {
		return getGenericParameterType(methodParameter, Collection.class, 0);
	}
	
	public static Class<?> getGenericParameterType(MethodParameter methodParam, Class<?> source, int typeIndex) {
		return extractType(methodParam, GenericTypeResolver.getTargetType(methodParam), source, typeIndex,
				methodParam.getNestingLevel(), 1);
	}
	
	/**
	 * 提取类型
	 * @param methodParam
	 * @param type
	 * @param source
	 * @param typeIndex
	 * @param nestingLevel
	 * @param currentLevel
	 * @return
	 */
	public static Class<?> extractType(MethodParameter methodParam, Type type, Class<?> source, 
			int typeIndex, int nestingLevel, int currentLevel) {
		
		Type resolvedType = type;
		
		// 如果type是泛型T
		if (resolvedType instanceof TypeVariable && methodParam != null && methodParam.getTypeVariableMap() != null) {
			Type mappedType = methodParam.getTypeVariableMap().get((TypeVariable<?>) type);
			if (mappedType != null) {
				resolvedType = mappedType;
			}
		}
		
		// 如果resolvedType是带泛型的类型，如List<T>
		if (resolvedType instanceof ParameterizedType) {
			return extractTypeFromParameterizedType(methodParam, (ParameterizedType) resolvedType, source, typeIndex,
					nestingLevel, currentLevel);
		}
		
		// 如果resolvedType是普通的Class
		if (resolvedType instanceof Class) {
			return extractTypeFromClass(methodParam, (Class<?>) resolvedType, source, typeIndex,
					nestingLevel, currentLevel);
		}
		
		return null;
	}
	
	public static Class<?> getCollectionFieldType(Field collectionField) {
		return getGenericFieldType(collectionField, Collection.class, 0, 1);
	}
	
	public static Class<?> getGenericFieldType(Field field, Class<?> source, int typeIndex, int nestingLevel) {
		return extractType(null, field.getGenericType(), source, typeIndex, nestingLevel, 1);
	}
	
	public static Class<?> getMapKeyFieldType(Field mapKeyField) {
		return getGenericFieldType(mapKeyField, Map.class, 0, 1);
	}
	
	public static Class<?> getMapKeyParameterType(MethodParameter mapKeyMethodParameter) {
		return getGenericParameterType(mapKeyMethodParameter, Map.class, 0);
	}

	public static Class<?> getMapValueFieldType(Field mapValueField) {
		return getGenericFieldType(mapValueField, Map.class, 1, 1);
	}
	
	public static Class<?> getMapValueParameterType(MethodParameter mapValueMethodParameter) {
		return getGenericParameterType(mapValueMethodParameter, Map.class, 1);
	}
	
	@SuppressWarnings("rawtypes")
	private static Class<?> extractTypeFromParameterizedType(MethodParameter methodParam, ParameterizedType ptype, 
			Class<?> source, int typeIndex, int nestingLevel, int currentLevel) {

		// ptype.getRawType 得到的是泛型的容器类型，如List
		if (!(ptype.getRawType() instanceof Class)) {
			return null;
		}
		
		// 该方法只处理 Class<T> 
		Class<?> rawType = (Class<?>) ptype.getRawType();
		
		// 获取泛型的实际类型
		Type[] paramTypes = ptype.getActualTypeArguments();
		
		if (nestingLevel - currentLevel > 0) {
			int nextLevel = currentLevel + 1;
			Integer currentTypeLevel = methodParam != null ? methodParam.getTypeIndexForLevel(nextLevel) : null;
			int indexToUse = currentTypeLevel != null ? currentTypeLevel : paramTypes.length - 1;
			Type paramType = paramTypes[indexToUse];
			return extractType(methodParam, paramType, source, typeIndex, nestingLevel, nextLevel);
		}
		
		// source不是rawType的父类或父接口时，返空
		if (source != null && !source.isAssignableFrom(rawType)) {
			return null;
		}
		
		Class<?> fromSuperClassOrInterface = extractTypeFromClass(methodParam, rawType, source, typeIndex, nestingLevel, currentLevel);
		if (fromSuperClassOrInterface != null) {
			return fromSuperClassOrInterface;
		}
		
		if (paramTypes == null || typeIndex >= paramTypes.length) {
			return null;
		}
		
		Type paramType = paramTypes[typeIndex];
		
		// paramType是有名称的泛型，如T、K、V
		if (paramType instanceof TypeVariable && methodParam != null && methodParam.getTypeVariableMap() != null) {
			Type mappedType = methodParam.getTypeVariableMap().get((TypeVariable) paramType);
			if (mappedType != null) {
				paramType = mappedType;
			}
		}
		
		// paramType是带通配符？的泛型
		if (paramType instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) paramType;
			// 上边界 ? extends List
			Type[] upperBounds = wildcardType.getUpperBounds();
			if (upperBounds != null && upperBounds.length > 0 && !Object.class.equals(upperBounds[0])) {
				paramType = upperBounds[0];
			} else {
				Type[] lowerBounds = wildcardType.getLowerBounds();
				if (lowerBounds != null && lowerBounds.length > 0 && !Object.class.equals(lowerBounds[0])) {
					paramType = lowerBounds[0];
				}
			}
		}
		
		// paramType是可带泛型的类型
		if (paramType instanceof ParameterizedType) {
			paramType = ((ParameterizedType) paramType).getRawType();
		}
		
		// paramType是数组类型
		if (paramType instanceof GenericArrayType) {
			Type comType = ((GenericArrayType) paramType).getGenericComponentType();
			if (comType instanceof Class) {
				return Array.newInstance((Class) comType, 0).getClass();
			}
		} else if (paramType instanceof Class) {
			// paramType是普通类型。Class就代表是String、Integer等普通类型
			return (Class) paramType;
		}
		return null;
		
	}
	
	
	private static Class<?> extractTypeFromClass(MethodParameter methodParam, Class<?> clazz, 
			Class<?> source, int typeIndex, int nestingLevel, int currentLevel) {
		
		if (clazz.getName().startsWith("java.util")) {
			return null;
		}
		
		if (clazz.getSuperclass() != null && isInstrospectionCandidate(clazz.getSuperclass())) {
			return extractType(methodParam, clazz.getGenericSuperclass(), source, typeIndex, nestingLevel, currentLevel);
		}
		
		Type[] iftps = clazz.getGenericInterfaces();
		if (iftps != null) {
			for (Type iftp : iftps) {
				Type rawType = iftp;
				if (iftp instanceof ParameterizedType) {
					rawType = ((ParameterizedType) iftp).getRawType();
				}
				if (rawType instanceof Class<?> && isInstrospectionCandidate((Class<?>) rawType)) {
					return extractType(methodParam, iftp, source, typeIndex, nestingLevel, currentLevel);
				}
			}
		}
		return null;
	}
	
	private static boolean isInstrospectionCandidate(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
	}
}
