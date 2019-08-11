package org.spex.web.context.support;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.web.context.ServletConfigAware;
import org.spex.web.context.ServletContextAware;

public class ServletContextAwareProcessor implements BeanPostProcessor {

	private ServletContext servletContext;
	
	private ServletConfig servletConfig;
	
	public ServletContextAwareProcessor(ServletContext servletContext) {
		this(servletContext, null);
	}

	public ServletContextAwareProcessor(ServletConfig servletConfig) {
		this(null, servletConfig);
	}

	public ServletContextAwareProcessor(ServletContext servletContext, ServletConfig servletConfig) {
		this.servletContext = servletContext;
		this.servletConfig = servletConfig;
		if (servletContext == null && servletConfig != null) {
			this.servletContext = servletConfig.getServletContext();
		}
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (this.servletContext != null && bean instanceof ServletContextAware) {
			((ServletContextAware) bean).setServletContext(servletContext);
		}
		if (this.servletConfig != null && bean instanceof ServletConfigAware) {
			((ServletConfigAware) bean).setServletConfig(servletConfig);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
