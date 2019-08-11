package org.spex.web.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.spex.context.ConfigurableApplicationContext;

public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	
	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";
	
	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";
	
	void setServletContext(ServletContext servletContext);
	
	void setConfigLocation(String configLocation);
	
	void setServletConfig(ServletConfig servletConfig);
	
	void setNamespace(String namespace);
}
