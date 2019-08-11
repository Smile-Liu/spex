package org.spex.context.event;

import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;
import org.spex.core.Ordered;

public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

	boolean supportEventType(Class<? extends ApplicationEvent> eventType);
	
	
	boolean supportSourceType(Class<?> sourceType);
}
