package org.spex.web.context;

import javax.servlet.ServletContext;

import org.spex.context.ApplicationContext;

public interface WebApplicationContext extends ApplicationContext {

	String ROOT_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";
	
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";
	
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";
	
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";
	
	ServletContext getServletContext();
}
