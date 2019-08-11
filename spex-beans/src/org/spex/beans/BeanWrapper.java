package org.spex.beans;

import java.beans.PropertyDescriptor;

public interface BeanWrapper extends PropertyAccessor, TypeConverter, PropertyEditorRegistry {

	Object getWrappedInstance();
	
	
	Class<?> getWrappedClass();
	
	
	PropertyDescriptor[] getPropertyDescriptors();
	
	
	/**
	 * Spring里的该方法支持嵌套的属性名，例如a.b
	 * 在这里先暂不支持
	 * @param propertyName
	 * @return
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName);
}
