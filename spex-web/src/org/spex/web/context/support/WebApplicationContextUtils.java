package org.spex.web.context.support;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.spex.beans.BeansException;
import org.spex.beans.factory.ListableBeanFactory;
import org.spex.beans.factory.ObjectFactory;
import org.spex.web.context.ConfigurableWebApplicationContext;
import org.spex.web.context.WebApplicationContext;
import org.spex.web.context.request.WebRequest;

public class WebApplicationContextUtils {

	public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
		return getWebApplicationContext(sc, WebApplicationContext.ROOT_APPLICATION_CONTEXT_ATTRIBUTE);
	}
	
	public static WebApplicationContext getWebApplicationContext(ServletContext sc, String attributeName) {
		Object attr = sc.getAttribute(attributeName);
		if (attr == null) {
			return null;
		}
		if (attr instanceof RuntimeException) {
			throw (RuntimeException) attr;
		}
		if (attr instanceof Exception) {
			throw new IllegalStateException((Exception) attr);
		}
		if (!(attr instanceof WebApplicationContext)) {
			throw new IllegalStateException("²»ÊÇWebApplicationContext");
		}
		return (WebApplicationContext) attr;
	}
	
	
	public static void registerWebApplicationScopes(ListableBeanFactory beanFactory, ServletContext sc) {
		// scope
		
		
		// ×¢²áHTTPµÄResolvableDependency
		beanFactory.registerResolvableDependency(ServletRequest.class, new RequestObjectFactory());
		beanFactory.registerResolvableDependency(HttpSession.class, new SessionObjectFactory());
		beanFactory.registerResolvableDependency(WebRequest.class, new WebRequestObjectFactory());
	}
	
	public static void registerEnvironmentBeans(ListableBeanFactory bf, ServletContext sc, ServletConfig so) {
		if (sc != null && !bf.containsBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
			bf.registerSingleton(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, sc);
		}
		if (so != null && !bf.containsBean(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
			bf.registerSingleton(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, so);
		}
		if (!bf.containsBean(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
			Map<String, String> paramMap = new HashMap<String, String>();
			if (sc != null) {
				Enumeration<?> initParameterNames = sc.getInitParameterNames();
				while (initParameterNames.hasMoreElements()) {
					String paramName = (String) initParameterNames.nextElement();
					paramMap.put(paramName, sc.getInitParameter(paramName));
				}
			}
			if (so != null) {
				Enumeration<?> initParameterNames = so.getInitParameterNames();
				while (initParameterNames.hasMoreElements()) {
					String paramName = (String) initParameterNames.nextElement();
					paramMap.put(paramName, so.getInitParameter(paramName));
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME, paramMap);
		}
		if (!bf.containsBean(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
			Map<String, Object> attributeMap = new HashMap<String, Object>();
			if (sc != null) {
				Enumeration<?> attributeNames = sc.getAttributeNames();
				if (attributeNames.hasMoreElements()) {
					String attributeName = (String) attributeNames.nextElement();
					attributeMap.put(attributeName, sc.getAttribute(attributeName));
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME, attributeMap);
		}
	}
	
	private static class RequestObjectFactory implements ObjectFactory<ServletRequest>, Serializable {

		private static final long serialVersionUID = 2617213418208248676L;

		@Override
		public ServletRequest getObject() throws BeansException {
			return null;
		}
		
		public String toString() {
			return "Current HttpServletRequest";
		}
	}
	
	private static class SessionObjectFactory implements ObjectFactory<HttpSession>, Serializable {

		private static final long serialVersionUID = 5883819796180484371L;

		@Override
		public HttpSession getObject() throws BeansException {
			return null;
		}
		
		public String toString() {
			return "Current HttpSession";
		}
	}
	
	private static class WebRequestObjectFactory implements ObjectFactory<WebRequest>, Serializable {

		private static final long serialVersionUID = -9015185960408705016L;

		@Override
		public WebRequest getObject() throws BeansException {
			return null;
		}
		
		public String toString() {
			return "Current ServletWebRequest";
		}
	}
}
