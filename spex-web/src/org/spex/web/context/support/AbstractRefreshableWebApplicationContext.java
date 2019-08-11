package org.spex.web.context.support;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.spex.beans.factory.ListableBeanFactory;
import org.spex.context.support.AbstractRefreshableConfigApplicationContext;
import org.spex.ui.context.Theme;
import org.spex.ui.context.ThemeSource;
import org.spex.ui.context.UiApplicationContextUtils;
import org.spex.web.context.ConfigurableWebApplicationContext;

public abstract class AbstractRefreshableWebApplicationContext extends AbstractRefreshableConfigApplicationContext
		implements ConfigurableWebApplicationContext, ThemeSource {

	private ServletContext servletContext;
	
	private ServletConfig servletConfig;
	
	private String namespace;
	
	private ThemeSource themeSource;
	
	public AbstractRefreshableWebApplicationContext() {
		setDisplayName("Root WebApplicationContext");
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
		if (servletConfig != null && this.servletContext == null) {
			this.servletContext = servletConfig.getServletContext();
		}
	}

	protected void postProcessBeanFactory(ListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
		
		WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
		WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
	}

	protected String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@Override
	public void onRefresh() {
		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	}
	
	@Override
	public Theme getTheme(String themeName) {
		return this.themeSource.getTheme(themeName);
	}
}
