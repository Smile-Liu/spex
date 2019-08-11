package org.spex.ui;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.spex.core.Conventions;

public class ModelMap extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = -2381969755021234384L;

	public ModelMap() {
	}

	public ModelMap(String attributeName, Object attributeValue) {
		addAttribute(attributeName, attributeValue);
	}
	
	public ModelMap(Object attributeValue) {
		
	}

	public ModelMap addAttribute(String attributeName, Object attributeValue) {
		if (attributeName == null) {
			throw new IllegalArgumentException("Model attribute name must not be null");
		}
		put(attributeName, attributeValue);
		return this;
	}
	
	public ModelMap addAttribute(Object attributeValue) {
		if (attributeValue == null) {
			throw new IllegalArgumentException("Model object must not be null");
		}
		if (attributeValue instanceof Collection && ((Collection<?>) attributeValue).isEmpty()) {
			return this;
		}
		return addAttribute(Conventions.getVariableName(attributeValue), attributeValue);
	}
}
