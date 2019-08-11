package org.spex.beans.factory.config;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.spex.core.GenericCollectionTypeResolver;
import org.spex.core.MethodParameter;
import org.spex.core.ParameterNameDiscoverer;

/**
 * һ����Ҫ��ע���������������
 * ���װ�Ŀ����ǹ�����������Ҳ�����Ƿ���������Ҳ�������ֶ�
 * ����ͳһ�ط������ǵ�Ԫ����
 * ע��transient�ؼ�����ָ���ֶβ����뵽���л�������
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
	 * Ϊ�����������򷽷���������������
	 * @param methodParameter Ҫ��װ�Ĳ���
	 * @param required �Ƿ�����������Ǳ����
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
		this(methodParameter, required, true);
	}
	
	/**
	 * Ϊ�����������򷽷���������������
	 * @param methodParameter Ҫ��װ�Ĳ���
	 * @param required �Ƿ�����������Ǳ����
	 * @param eager �Ƿ���������Ǽ��еģ������е����ڽ�������ƥ���beans
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
