package org.spex.beans;

import java.beans.PropertyChangeEvent;

import org.spex.util.ClassUtils;

public class TypeMismatchException extends PropertyAccessException {

	private static final long serialVersionUID = 1L;

	private transient Object value;
	
	private Class<?> requiredType;
	
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType) {
		this(propertyChangeEvent, requiredType, null);
	}
	
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType, Throwable ex) {
		super(propertyChangeEvent, "��������'" + ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue()) + "'��ֵת��" +
				(requiredType != null ? "Ϊ����'" + ClassUtils.getQualifiedName(requiredType) + "'" : "") +
				"ʧ��", ex);
		this.value = propertyChangeEvent.getNewValue();
		this.requiredType = requiredType;
	}
}
