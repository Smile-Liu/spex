package org.spex.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.spex.core.BridgeMethodResolver;
import org.spex.core.GenericTypeResolver;
import org.spex.core.MethodParameter;
import org.spex.util.ClassUtils;
import org.spex.util.StringUtils;

public class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {

	private final Class<?> beanClass;
	
	private final Method readMethod;
	
	private final Method writeMethod;
	
	private final Class<?> propertyEditorClass;
	
	private volatile Set<Method> ambiguousWriteMethods;
	
	private Class<?> propertyType;
	
	private MethodParameter writeMethodParameter;
	
	public GenericTypeAwarePropertyDescriptor(Class<?> beanClass, String propertyName, 
			Method readMethod, Method writeMethod, Class<?> propertyEditorClass) throws IntrospectionException {
		
		super(propertyName, beanClass, null, null);
		this.beanClass = beanClass;
		this.propertyEditorClass = propertyEditorClass;
		
		Method readMethodToUse = BridgeMethodResolver.findBridgedMethod(readMethod);
		Method writeMethodToUse = BridgeMethodResolver.findBridgedMethod(writeMethod);
		
		if (writeMethodToUse == null && readMethodToUse != null) {
			// ͨ��PropertyDescriptor�õ���writeMethod�Ǹ��Žӷ������÷����ǲ����õ�
			// ��鿴�����Ƿ������readMethod�ķ�������Ϊ�������͵�writeMethod
			writeMethodToUse = ClassUtils.getMethodIfAvailable(beanClass, 
					"set" + StringUtils.capitalize(getName()), readMethodToUse.getReturnType());
		}
		this.readMethod = readMethodToUse;
		this.writeMethod = writeMethodToUse;
		
		if (this.writeMethod != null && this.readMethod == null) {
			// getter��setter��ƥ�䣬���¼Ϊ���������
			Set<Method> ambiguousCandidates = new HashSet<Method>();
			
			for (Method method : beanClass.getMethods()) {
				if (method.getName().equals(writeMethodToUse.getName()) &&
						!method.equals(writeMethodToUse) && !method.isBridge()) {
					ambiguousCandidates.add(method);
				}
			}
			
			if (!ambiguousCandidates.isEmpty()) {
				this.ambiguousWriteMethods = ambiguousCandidates;
			}
		}
	}
	
	public synchronized MethodParameter getWriterMethodParameter() {
		if (this.writeMethod == null) {
			return null;
		}
		if (this.writeMethodParameter == null) {
			this.writeMethodParameter = new MethodParameter(this.writeMethod, 0);
			GenericTypeResolver.resolveParameterType(this.writeMethodParameter, this.beanClass);
		}
		return this.writeMethodParameter;
	}
	
	@Override
	public Method getReadMethod() {
		return this.readMethod;
	}
	
	@Override
	public Method getWriteMethod() {
		return this.writeMethod;
	}
}
