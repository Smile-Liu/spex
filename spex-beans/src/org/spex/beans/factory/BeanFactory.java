package org.spex.beans.factory;

/**
 * BeanFactory�ܽӿ�
 * �ṩ��ȡBean�ĸ��ַ���
 * ����Bean��������Ĺ��췽����ʵ�ֵ�
 * @author hp
 */
public interface BeanFactory {
	
	String FACTORY_BEAN_PREFIX = "&";
	
	/**
	 * ͨ��BeanName��ȡBeanʵ��
	 * ��֧��name����֧�ֱ���
	 * @param beanName �����ļ���ָ����Bean����
	 * @return Beanʵ��
	 */
	Object getBean(String beanName);
	
	
	<T> T getBean(String beanName, Class<T> requiredType);
	
	/**
	 * ͨ��classtype��ȡBeanʵ��
	 * ��֧�������ļ��е�Class
	 * @param beanClass �����ļ��е�Class
	 * @return Beanʵ������
	 */
	<T> T getBean(Class<T> beanClass);
	
	
	/**
	 * �ж��Ƿ����ָ��BeanName��Bean
	 * ��֧��name����֧�ֱ���
	 * @param beanName �����ļ���ָ����Bean����
	 * @return �Ƿ����
	 */
	boolean containsBean(String beanName);
	
	
	/**
	 * �������ƣ�bean name, factory bean name����ȡע���Class
	 * @param name ����
	 * @return
	 */
	Class<?> getType(String name);
	
	
	boolean isTypeMatch(String name, Class<?> targetType);
}
