package org.spex.context.event;

import org.spex.context.ApplicationContext;

public class ContextStoppedEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -1047347916749286890L;

	public ContextStoppedEvent(ApplicationContext source) {
		super(source);
	}
}
