package org.spex.beans;


public interface PropertyAccessor {

	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';
	
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';
	
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';
	
	
	void setPropertyValues(PropertyValues pvs) throws BeansException;
	
	
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException;
	
	
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, 
			boolean ignoreInvalid) throws BeansException;
	
	
	boolean isWritableProperty(String propertyName);
	
	
	Object getPropertyValue(String propertyName) throws BeansException;
}
