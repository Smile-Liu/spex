package org.spex.beans;

public class PropertyAccessorFactory {

	
	public static BeanWrapper forBeanPropertyAccess(Object target) {
		return new BeanWrapperImpl(target);
	}
}
