package org.spex.beans.factory.support;

import java.beans.PropertyDescriptor;

import org.spex.core.MethodParameter;
import org.spex.core.convert.TypeDescriptor;

public class PropertyTypeDescriptor extends TypeDescriptor {

	private final PropertyDescriptor propertyDescriptor;
	
	public PropertyTypeDescriptor(PropertyDescriptor propertyDescriptor, MethodParameter methodParam) {
		super(methodParam);
		this.propertyDescriptor = propertyDescriptor;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}
	
}
