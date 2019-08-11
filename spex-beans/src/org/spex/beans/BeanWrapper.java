package org.spex.beans;

import java.beans.PropertyDescriptor;

public interface BeanWrapper extends PropertyAccessor, TypeConverter, PropertyEditorRegistry {

	Object getWrappedInstance();
	
	
	Class<?> getWrappedClass();
	
	
	PropertyDescriptor[] getPropertyDescriptors();
	
	
	/**
	 * Spring��ĸ÷���֧��Ƕ�׵�������������a.b
	 * ���������ݲ�֧��
	 * @param propertyName
	 * @return
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName);
}
