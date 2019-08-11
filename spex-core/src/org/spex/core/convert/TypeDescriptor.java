package org.spex.core.convert;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.spex.core.MethodParameter;

public class TypeDescriptor {

	
	private Object value;
	
	private Class<?> type;
	
	private MethodParameter methodParameter;
	
	private Field field;

	public TypeDescriptor() {
	}
	
	public TypeDescriptor(MethodParameter methodParameter) {
		this.methodParameter = methodParameter;
	}
	
	public TypeDescriptor(MethodParameter methodParameter, Class<?> type) {
		this.methodParameter = methodParameter;
		this.type = type;
	}
	
	public TypeDescriptor(Field field) {
		this.field = field;
	}
	
	public TypeDescriptor(Field field, Class<?> type) {
		this.field = field;
		this.type = type;
	}
	
	public TypeDescriptor(Object value) {
		this.value = value;
		this.type = value.getClass();
	}
	
	private TypeDescriptor(Class<?> type) {
		this.type = type;
	}
	
	public TypeDescriptor forElementType(Class<?> elementType) {
		if (getType().equals(elementType)) {
			return this;
		}
		if (this.methodParameter != null) {
			return new TypeDescriptor(methodParameter, elementType);
		}
		return new TypeDescriptor(field, elementType);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getType() {
		if (this.type != null) {
			return this.type;
		}
		if (this.field != null) {
			return this.field.getType();
		}
		if (this.methodParameter != null) {
			return this.methodParameter.getParameterType();
		}
		return null;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public MethodParameter getMethodParameter() {
		return methodParameter;
	}

	public void setMethodParameter(MethodParameter methodParameter) {
		this.methodParameter = methodParameter;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
	
	

	public static TypeDescriptor forObject(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Collection || obj instanceof Map) {
			return new TypeDescriptor(obj);
		}
		return new TypeDescriptor(obj.getClass());
	}
	
	public static TypeDescriptor valueOf(Class<?> type) {
		if (type == null) {
			return new TypeDescriptor();
		}
		return new TypeDescriptor(type);
	}
	
}
