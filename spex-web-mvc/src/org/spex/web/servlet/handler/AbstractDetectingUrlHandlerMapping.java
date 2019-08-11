package org.spex.web.servlet.handler;

import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.context.ApplicationContext;
import org.spex.util.LoggerUtil;

public abstract class AbstractDetectingUrlHandlerMapping extends AbstractUrlHandlerMapping {

	private boolean detectHandlersInAncestorContexts = false;

	public void setDetectHandlersInAncestorContexts(boolean detectHandlersInAncestorContexts) {
		this.detectHandlersInAncestorContexts = detectHandlersInAncestorContexts;
	}
	
	@Override
	protected void initApplicationContext(ApplicationContext context) {
		super.initApplicationContext(context);
		detectHandlers();
	}
	
	protected void detectHandlers() {
		LoggerUtil.info("ÔÚapplication contextÖÐ²éÕÒurl mapping");
		
		String[] beanNames = this.detectHandlersInAncestorContexts ?
				BeanFactoryUtils.beanNameForTypeIncludingAncestors(getApplicationContext(), Object.class, true, true) :
				getApplicationContext().getBeanNamesForType(Object.class, true, true);
				
		for (String beanName : beanNames) {
			String[] urls = determineUrlsForHandler(beanName);
			if (urls != null && urls.length > 0) {
				registerHandler(urls, beanName);
			}
		}
	}
	
	
	protected abstract String[] determineUrlsForHandler(String beanName);
}
