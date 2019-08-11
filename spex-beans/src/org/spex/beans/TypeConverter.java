package org.spex.beans;

import org.spex.core.MethodParameter;

public interface TypeConverter {

	<T> T convertIfNecessary(Object value, Class<?> requiredType);
	
	<T> T convertIfNecessary(Object value, Class<?> requiredType, MethodParameter methodParam);
}
