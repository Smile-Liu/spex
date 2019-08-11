package org.spex.context.event;

import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;

public interface ApplicationEventMulticaster {

	void addApplicationListener(ApplicationListener<?> listener);
	
	void addApplicationListenerBean(String listenerName);
	
	void removeApplicationListener(ApplicationListener<?> listener);
	
	void removeApplicationListenerBean(String listenerName);
	
	void removeAllListeners();
	
	void multicastEvent(ApplicationEvent event);
}
