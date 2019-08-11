package org.spex.beans;

public class NullValueInNestedPathException extends InvalidPropertyException {

	private static final long serialVersionUID = 1L;

	public NullValueInNestedPathException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "Ƕ������'" + propertyName + "'��ֵ�ǿգ�Null����");
	}
}
