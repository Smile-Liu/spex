package org.spex.context.event;

import org.spex.context.ApplicationContext;

public class ContextRefreshedEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -1047347916749286890L;

	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}
}
