package org.spex.beans.factory.config;

import org.spex.util.ClassUtils;

public class TypedStringValue {

	private String value;
	private Object targetType;
	
	private boolean dynamic;
	
	private Object source;
	
	public TypedStringValue(String value) {
		this.value = value;
	}
	
	public TypedStringValue(String value, Object targetType) {
		this.value = value;
		this.targetType = targetType;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public boolean hasTargetType() {
		return this.targetType instanceof Class;
	}
	
	public Class<?> resolveTargetType(ClassLoader cl) throws ClassNotFoundException {
		if (this.targetType == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(getTargetTypeName(), cl);
		this.targetType = resolvedClass;
		return resolvedClass;
	}
	
	public String getTargetTypeName() {
		if (this.targetType instanceof Class) {
			return ((Class<?>)this.targetType).getName();
		} else {
			return (String)this.targetType;
		}
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Class<?> getTargetType() {
		if (!(this.targetType instanceof Class)) {
			throw new IllegalStateException("TypedStringValue类没有指定具体类型");
		}
		return (Class<?>) this.targetType;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public void setTargetType(Object targetType) {
		this.targetType = targetType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = ((value == null) ? 0 : prime * value.hashCode() + 
				((targetType == null) ? 0 : targetType.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedStringValue other = (TypedStringValue) obj;
		if (targetType == null) {
			if (other.targetType != null)
				return false;
		} else if (!targetType.equals(other.targetType))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
