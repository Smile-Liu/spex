package org.spex.beans.factory.config;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.spex.core.GenericCollectionTypeResolver;
import org.spex.core.MethodParameter;
import org.spex.core.ParameterNameDiscoverer;

/**
 * 一个将要被注入的依赖的描述器
 * 其包装的可以是构造器参数、也可以是方法参数、也可以是字段
 * 允许统一地访问它们的元数据
 * 注：transient关键字是指定字段不参与到序列化过程中
 * @author hp
 */
public class DependencyDescriptor implements Serializable {

	private static final long serialVersionUID = 946000867442074845L;

	
	private transient MethodParameter methodParameter;
	private transient Field field;
	
	private Class<?> declaringClass;
	
	private String methodName;
	
	private Class<?>[] parameterTypes;
	
	private int parameterIndex;
	
	private String fieldName;
	
	private final boolean required;
	private final boolean eager;
	
	/**
	 * 为构造器参数或方法参数创建描述器
	 * @param methodParameter 要包装的参数
	 * @param required 是否这个描述器是必须的
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
		this(methodParameter, required, true);
	}
	
	/**
	 * 为构造器参数或方法参数创建描述器
	 * @param methodParameter 要包装的参数
	 * @param required 是否这个描述器是必须的
	 * @param eager 是否该描述器是急切的，即急切的用于解析类型匹配的beans
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
		this.methodParameter = methodParameter;
		this.declaringClass = methodParameter.getDeclaringClass();
		if (this.methodParameter.getMethod() != null) {
			this.methodName = methodParameter.getMethod().getName();
			this.parameterTypes = methodParameter.getMethod().getParameterTypes();
		} else {
			this.parameterTypes = methodParameter.getConstructor().getParameterTypes();
		}
		this.parameterIndex = methodParameter.getParameterIndex();
		this.required = required;
		this.eager = eager;
	}
	
	public void initParameterNameDiscover(ParameterNameDiscoverer parameterNameDiscoverer) {
		if (this.methodParameter != null) {
			this.methodParameter.initParameterNameDiscoverer(parameterNameDiscoverer);
		}
	}
	
	public Class<?> getDependencyType() {
		return this.field != null ? this.field.getType() : this.methodParameter.getParameterType();
	}
	
	public Type getGenericDependencyType() {
		return this.field != null ? this.field.getGenericType() : this.methodParameter.getGenericParameterType();
	}

	public boolean isEager() {
		return eager;
	}

	public boolean isRequired() {
		return required;
	}
	
	public Class<?> getCollectionType() {
		return this.field != null ? GenericCollectionTypeResolver.getCollectionFieldType(this.field) :
			GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter);
	}
	
	public Class<?> getMapKeyType() {
		return this.field != null ? GenericCollectionTypeResolver.getMapKeyFieldType(this.field) :
			GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter);
	}
	
	public Class<?> getMapValueType() {
		return this.field != null ? GenericCollectionTypeResolver.getMapValueFieldType(this.field) :
			GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter);
	}
	
	public String getDependencyName() {
		return this.field != null ? this.field.getName() : this.methodParameter.getParameterName();
	}
}
