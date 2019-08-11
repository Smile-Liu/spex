package org.spex.context;

import org.spex.beans.factory.ListableBeanFactory;

public interface ApplicationContext extends ListableBeanFactory, ApplicationEventPublisher {

	/**
	 * 返回Context的唯一标识
	 * @return context的唯一标识
	 */
	String getId();
	
	/**
	 * Context的显示名称
	 * @return context的显示名称
	 */
	String getDisplayName();
	
	/**
	 * 启动时间戳
	 * @return 启动时间戳
	 */
	long getStartupDate();
	
	ListableBeanFactory getListableBeanFactory();
}
