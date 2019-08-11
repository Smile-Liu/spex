package org.spex.beans;

import java.beans.PropertyChangeEvent;

public class MethodInvocationException extends PropertyAccessException {

	private static final long serialVersionUID = 1L;

	public MethodInvocationException(PropertyChangeEvent propertyChangeEvent, Throwable ex) {
		super(propertyChangeEvent, "����'" + propertyChangeEvent.getPropertyName() + "'�׳��쳣", ex);
	}
}
