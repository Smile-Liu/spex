package org.spex.beans;

public class NotWritablePropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 1L;

	public NotWritablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "����'" + propertyName + "'�ǲ���д�Ļ�û����Ч��setter����" +
				"��ȷ��getter�ķ���ֵ�����Ƿ�ƥ��setter�Ĳ�������");
	}
	
	public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}
}
