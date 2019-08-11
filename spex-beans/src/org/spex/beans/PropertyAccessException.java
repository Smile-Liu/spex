package org.spex.beans;

import java.beans.PropertyChangeEvent;

public class PropertyAccessException extends BeansException {
	
	private static final long serialVersionUID = 1L;

	private transient PropertyChangeEvent propertyChangeEvent;
	
	public PropertyAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, Throwable ex) {
		super(msg, ex);
		this.propertyChangeEvent = propertyChangeEvent;
	}
}
