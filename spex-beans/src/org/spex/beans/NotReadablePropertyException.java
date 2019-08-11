package org.spex.beans;

public class NotReadablePropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 1L;

	public NotReadablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "����'" + propertyName + "'�ǲ��ɶ��Ļ�û����Ч��getter����" +
				"��ȷ��getter�ķ���ֵ�����Ƿ�ƥ��setter�Ĳ�������");
	}
	
	public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}
}
