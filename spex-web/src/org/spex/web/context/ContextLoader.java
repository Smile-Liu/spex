package org.spex.web.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.spex.beans.BeanUtils;
import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationContextException;
import org.spex.util.ClassUtils;
import org.spex.util.LoggerUtil;

public class ContextLoader {

	private static final String CONTEXT_CLASS_PARAM = "contextClass";
	
	private static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	
	private static final String DEFAULT_STRATEGIES_PATH = "ContextLoader.properties";
	
	private static final Properties defaultStrategies;
	
	static {
		try {
			InputStream is = ContextLoader.class.getResourceAsStream(DEFAULT_STRATEGIES_PATH);
			defaultStrategies = new Properties();
			defaultStrategies.load(is);
		} catch (IOException e) {
			throw new IllegalStateException("不能加载'ContextLoader.properties'文件:" + e.getMessage());
		}
	}
	
	private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread = 
		new ConcurrentHashMap<ClassLoader, WebApplicationContext>(1);
	
	private static volatile WebApplicationContext currentContext;
	
	private WebApplicationContext context;
	
	/**
	 * 初始化时调用
	 * @param servletContext
	 * @return
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		if (servletContext.getAttribute(WebApplicationContext.ROOT_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
			throw new IllegalStateException("已经创建过application context了。可以查看web.xml中是否有多份ContextLoader定义");
		}
		
		LoggerUtil.info("开始初始化根WebApplicationContext");
		
		long startTime = System.currentTimeMillis();
		
		ApplicationContext parent = loadParentContext(servletContext);
		
		this.context = createWebApplicationContext(servletContext, parent);
		servletContext.setAttribute(WebApplicationContext.ROOT_APPLICATION_CONTEXT_ATTRIBUTE, this.context);
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == ContextLoader.class.getClassLoader()) {
			currentContext = this.context;
		} else if (cl != null) {
			currentContextPerThread.put(cl, context);
		}
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		LoggerUtil.info("根Application Context实例化完成，耗时：" + elapsedTime);
		
		return this.context;
	}
	
	
	/**
	 * 销毁时调用
	 * @param servletContext
	 */
	public void closeWebApplicationContext(ServletContext servletContext) {
		
	}
	
	protected ApplicationContext loadParentContext(ServletContext servletContext) {
		ApplicationContext parent = null;
		return parent;
	}
	
	protected WebApplicationContext createWebApplicationContext(ServletContext sc, ApplicationContext parent) {
		Class<?> contextClass = determineContextClass(sc);
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("指定的Context类[" + contextClass.getName() + "]不是" + 
					ConfigurableWebApplicationContext.class.getName() + "类型");
		}
		
		ConfigurableWebApplicationContext wac = 
			(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		
		// Servlet版本小于2.5
		if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
			String servletContextName = sc.getServletContextName();
			wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName);
			
		} else {
			String contextPath = sc.getContextPath();
			wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + contextPath);
		}
		
		wac.setServletContext(sc);
		wac.setConfigLocation(sc.getInitParameter(CONFIG_LOCATION_PARAM));
		customizeContext(sc, wac);
		wac.refresh();
		return wac;
	}
	
	protected Class<?> determineContextClass(ServletContext servletContext) {
		String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		if (contextClassName != null) {
			try {
				return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
			} catch (ClassNotFoundException e) {
				throw new ApplicationContextException("加载自定义Context类[" + contextClassName + "]失败", e);
			}
			
		} else {
			contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
			try {
				return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new ApplicationContextException("加载默认的Context类[" + contextClassName + "]失败", e);
			}
		}
	}
	
	protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
		
	}
}
