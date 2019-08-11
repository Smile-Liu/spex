package org.spex.beans;

public class PropertyBatchUpdateException extends BeansException {

	private static final long serialVersionUID = 1L;

	private PropertyAccessException[] propertyAccessorExceptions;
	
	public PropertyBatchUpdateException(PropertyAccessException[] propertyAccessorExceptions) {
		super(null);
		this.propertyAccessorExceptions = propertyAccessorExceptions;
	}
	
	public String getMessage() {
		StringBuilder sb = new StringBuilder("Failed properties:");
		for (int i = 0; i < propertyAccessorExceptions.length; i++) {
			sb.append(propertyAccessorExceptions[i].getMessage());
			if (i < propertyAccessorExceptions.length - 1) {
				sb.append(";");
			}
		}
		return sb.toString();
	}
}
