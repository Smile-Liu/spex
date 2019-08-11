package org.spex.beans.factory;

public interface FactoryBean<T> {

	/**
	 * ���ر��ù������ɵ�Beanʵ��
	 * ������ø÷���ʱ����Ӧ��FactoryBean��δʵ����������׳��쳣
	 * ���з���null
	 * @return bean instance ������null��
	 * @throws Exception
	 */
	T getObject() throws Exception;
	
	
	/**
	 * ���ر��ù������ɵ�Bean������
	 * @return
	 */
	Class<?> getObjectType();
	
	
	/**
	 * �жϱ��ù������ɵ�Bean�Ƿ��ǵ���
	 * ����ǣ���ͨ�� {@link #getObject()} ÿ�η��صĶ���ͬһ��ʵ��
	 * @return �Ƿ��ǵ���
	 */
	boolean isSingleton();
}
