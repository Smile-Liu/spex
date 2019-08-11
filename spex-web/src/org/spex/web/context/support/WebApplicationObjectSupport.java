package org.spex.web.context.support;

import javax.servlet.ServletContext;

import org.spex.context.ApplicationContext;
import org.spex.context.support.ApplicationObjectSupport;
import org.spex.web.context.ServletContextAware;
import org.spex.web.context.WebApplicationContext;

public class WebApplicationObjectSupport extends ApplicationObjectSupport
		implements ServletContextAware {

	private ServletContext servletContext;
	
	@Override
	public final void setServletContext(ServletContext servletContext) {
		if (servletContext != this.servletContext) {
			this.servletContext = servletContext;
			if (servletContext != null) {
				initServletContext(servletContext);
			}
		}
	}

	@Override
	protected void initApplicationContext(ApplicationContext context) {
		super.initApplicationContext(context);
		if (this.servletContext == null && context instanceof WebApplicationContext) {
			this.servletContext = ((WebApplicationContext) context).getServletContext();
			if (this.servletContext != null) {
				initServletContext(this.servletContext);
			}
		}
	}
	
	protected void initServletContext(ServletContext servletContext) {}
	
	protected final WebApplicationContext getWebApplicationContext() {
		ApplicationContext context = getApplicationContext();
		if (context instanceof WebApplicationContext) {
			return (WebApplicationContext) context;
		} else {
			throw new IllegalStateException("WebApplicationObjectSupport需要WebApplicationContext，提供的却是[" + context + "]");
		}
	}
	
	protected final ServletContext getServletContext() {
		if (this.servletContext != null) {
			return this.servletContext;
		}
		ServletContext sc = getWebApplicationContext().getServletContext();
		if (sc == null) {
			throw new IllegalStateException("WebApplicationObjectSupport需要ServletContext");
		}
		return sc;
	}
}
