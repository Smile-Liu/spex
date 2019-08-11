package org.spex.context.support;

import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationContextAware;
import org.spex.context.ApplicationContextException;

public class ApplicationObjectSupport implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext context) {
		if (context != null) {
			if (this.applicationContext == null) {
				this.applicationContext = context;
				initApplicationContext(context);
				
			} else if (this.applicationContext != context) {
				throw new ApplicationContextException("不能重新给Application Context进行赋值");
			}
		}
	}
	
	protected Class<?> requiredContextClass() {
		return this.applicationContext.getClass();
	}

	protected void initApplicationContext(ApplicationContext context) {}
	
	public final ApplicationContext getApplicationContext() {
		if (this.applicationContext == null) {
			throw new IllegalStateException("ApplicationObjectSupport的ApplicationContext是空的");
		}
		return this.applicationContext;
	}
}
