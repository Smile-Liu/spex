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
			throw new IllegalStateException("���ܼ���'ContextLoader.properties'�ļ�:" + e.getMessage());
		}
	}
	
	private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread = 
		new ConcurrentHashMap<ClassLoader, WebApplicationContext>(1);
	
	private static volatile WebApplicationContext currentContext;
	
	private WebApplicationContext context;
	
	/**
	 * ��ʼ��ʱ����
	 * @param servletContext
	 * @return
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		if (servletContext.getAttribute(WebApplicationContext.ROOT_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
			throw new IllegalStateException("�Ѿ�������application context�ˡ����Բ鿴web.xml���Ƿ��ж��ContextLoader����");
		}
		
		LoggerUtil.info("��ʼ��ʼ����WebApplicationContext");
		
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
		LoggerUtil.info("��Application Contextʵ������ɣ���ʱ��" + elapsedTime);
		
		return this.context;
	}
	
	
	/**
	 * ����ʱ����
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
			throw new ApplicationContextException("ָ����Context��[" + contextClass.getName() + "]����" + 
					ConfigurableWebApplicationContext.class.getName() + "����");
		}
		
		ConfigurableWebApplicationContext wac = 
			(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		
		// Servlet�汾С��2.5
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
				throw new ApplicationContextException("�����Զ���Context��[" + contextClassName + "]ʧ��", e);
			}
			
		} else {
			contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
			try {
				return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new ApplicationContextException("����Ĭ�ϵ�Context��[" + contextClassName + "]ʧ��", e);
			}
		}
	}
	
	protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
		
	}
}
