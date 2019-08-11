package org.spex.context.event;

import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationEvent;

public class ApplicationContextEvent extends ApplicationEvent {

	private static final long serialVersionUID = -4867877175406134434L;

	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}
	
	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) super.getSource();
	}
}
