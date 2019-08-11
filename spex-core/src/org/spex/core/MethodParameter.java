package org.spex.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class MethodParameter {

	private Method method;
	
	private Constructor<?> constructor;
	
	private final int parameterIndex;
	
	private Class<?> parameterType;
	
	private String parameterName;
	
	private ParameterNameDiscoverer parameterNameDiscoverer;
	
	private int nestingLevel = 1;
	
	private Map<Integer, Integer> typeIndexesPerLevel;
	
	/** 泛型->类型 */
	private Map<TypeVariable<?>, Type> typeVariableMap;
	
	private Type genericParameterType;
	
	public MethodParameter(Method method, int paraterIndex) {
		this(method, paraterIndex, 1);
	}
	
	public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
		this.method = method;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
	}
	
	public MethodParameter(Constructor<?> ctor, int paraterIndex) {
		this(ctor, paraterIndex, 1);
	}
	
	public MethodParameter(Constructor<?> ctor, int parameterIndex, int nestingLevel) {
		this.constructor = ctor;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
	}
	
	public MethodParameter(MethodParameter original) {
		this.method = original.method;
		this.constructor = original.constructor;
		this.parameterIndex = original.parameterIndex;
		this.parameterName = original.parameterName;
		this.parameterType = original.parameterType;
		this.typeVariableMap = original.typeVariableMap;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public Class<?> getParameterType() {
		return parameterType;
	}

	public void setParameterType(Class<?> parameterType) {
		this.parameterType = parameterType;
	}

	public String getParameterName() {
		if (this.parameterNameDiscoverer != null) {
			String[] parameterNames = (this.method != null ? 
					this.parameterNameDiscoverer.getParameterNames(this.method) :
					this.parameterNameDiscoverer.getParameterNames(this.constructor));
			if (parameterNames != null) {
				this.parameterName = parameterNames[this.parameterIndex];
			}
		}
		return this.parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public int getNestingLevel() {
		return nestingLevel;
	}

	public void setNestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}

	public int getParameterIndex() {
		return parameterIndex;
	}

	public Map<TypeVariable<?>, Type> getTypeVariableMap() {
		return typeVariableMap;
	}

	public void setTypeVariableMap(Map<TypeVariable<?>, Type> typeVariableMap) {
		this.typeVariableMap = typeVariableMap;
	}

	public Map<Integer, Integer> getTypeIndexesPerLevel() {
		if (this.typeIndexesPerLevel == null) {
			this.typeIndexesPerLevel = new HashMap<Integer, Integer>(4);
		}
		return typeIndexesPerLevel;
	}
	
	public Integer getTypeIndexForLevel(int nestingLevel) {
		return this.getTypeIndexesPerLevel().get(nestingLevel);
	}
	
	public static MethodParameter forMethodOrConstructor(Object obj, int index) {
		if (obj instanceof Method) {
			return new MethodParameter((Method) obj, index);
		} else if (obj instanceof Constructor) {
			return new MethodParameter((Constructor<?>)obj, index);
		} else {
			throw new IllegalArgumentException("给定的对象 [" + obj + "] 既不是Method又不是Constructor");
		}
	}
	
	public Class<?> getDeclaringClass() {
		return this.method != null ? this.method.getDeclaringClass() : this.constructor.getDeclaringClass();
	}
	
	public void initParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}
	
	public Type getGenericParameterType() {
		if (this.genericParameterType == null) {
			if (this.parameterIndex < 0) {
				this.genericParameterType = this.method != null ? this.method.getGenericReturnType() : null;
			} else {
				this.genericParameterType = this.method != null ?
						this.method.getGenericParameterTypes()[this.parameterIndex] :
						this.constructor.getGenericParameterTypes()[this.parameterIndex];
			}
		}
		return this.genericParameterType;
	}

	public void increaseNestedLevel() {
		this.nestingLevel++;
	}

	public void decreaseNestedLevel() {
		getTypeIndexesPerLevel().remove(this.nestingLevel);
		this.nestingLevel--;
	}

	public void setTypeIndexForCurrentLevel(int typeIndex) {
		getTypeIndexesPerLevel().put(this.nestingLevel, typeIndex);
	}
}
