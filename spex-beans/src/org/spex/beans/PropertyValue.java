package org.spex.beans;

import java.beans.PropertyDescriptor;

public class PropertyValue {

	private Object value;
	private String name;
	
	private Object source;
	private boolean optional;
	private Object convertedValue;
	private boolean converted = false;
	
	// 同一Package可见
	volatile Boolean conversionNecessary;
	volatile Object resolvedTokens;
	volatile PropertyDescriptor resolvedDescriptor;
	
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public PropertyValue(PropertyValue original, Object newValue) {
		this.name = original.getName();
		this.value = newValue;
		this.source = original;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Object getConvertedValue() {
		return convertedValue;
	}

	public synchronized void setConvertedValue(Object convertedValue) {
		this.converted = true;
		this.convertedValue = convertedValue;
	}

	public boolean isConverted() {
		return converted;
	}

	public void setConverted(boolean converted) {
		this.converted = converted;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		while (original.source instanceof PropertyValue && original.source != original) {
			original = (PropertyValue) original.source;
		}
		return original;
	}
}
