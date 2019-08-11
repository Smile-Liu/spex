package org.spex.context;

import org.spex.beans.factory.ListableBeanFactory;

public interface ApplicationContext extends ListableBeanFactory, ApplicationEventPublisher {

	/**
	 * ����Context��Ψһ��ʶ
	 * @return context��Ψһ��ʶ
	 */
	String getId();
	
	/**
	 * Context����ʾ����
	 * @return context����ʾ����
	 */
	String getDisplayName();
	
	/**
	 * ����ʱ���
	 * @return ����ʱ���
	 */
	long getStartupDate();
	
	ListableBeanFactory getListableBeanFactory();
}
