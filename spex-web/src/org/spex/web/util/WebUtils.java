package org.spex.web.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class WebUtils {
	
	public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
	public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
	public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
	public static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info";
	public static final String INCLUDE_QUERY_STRING_ATTRIBUTE = "javax.servlet.include.query_string";

	public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";
	public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";
	public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";
	public static final String FORWARD_PATH_INFO_ATTRIBUTE = "javax.servlet.forward.path_info";
	public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";

	public static final String DEFAULT_CHARACTOR_ENCODING = "UTF-8";
	
	public static final String ERROR_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code";
	public static final String ERROR_EXCEPTION_TYPE_ATTRIBUTE = "javax.servlet.error.exception_type";
	public static final String ERROR_MESSAGE_ATTRIBUTE = "javax.servlet.error.message";
	public static final String ERROR_EXCEPTION_ATTRIBUTE = "javax.servlet.error.exception";
	public static final String ERROR_REQUEST_URI_ATTRIBUTE = "javax.servlet.error.request_uri";
	public static final String ERROR_SERVLET_NAME_ATTRIBUTE = "javax.servlet.error.servlet_name";
	
	public static String getSessionId(HttpServletRequest request) {
		if (request == null) return null;
		
		HttpSession session = request.getSession(false);
		return session != null ? session.getId() : null;
	}
	
	public static boolean isIncludeRequest(HttpServletRequest request) {
		return request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
	}
	
	public static void exposeErrorRequestAttributes(HttpServletRequest request, Throwable ex, String servletName) {
		exposeRequestAttributeIfNotPresent(request, ERROR_STATUS_CODE_ATTRIBUTE, HttpServletResponse.SC_OK);
		exposeRequestAttributeIfNotPresent(request, ERROR_EXCEPTION_TYPE_ATTRIBUTE, ex.getClass());
		exposeRequestAttributeIfNotPresent(request, ERROR_MESSAGE_ATTRIBUTE, ex);
		exposeRequestAttributeIfNotPresent(request, ERROR_EXCEPTION_ATTRIBUTE, HttpServletResponse.SC_OK);
		exposeRequestAttributeIfNotPresent(request, ERROR_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
		exposeRequestAttributeIfNotPresent(request, ERROR_SERVLET_NAME_ATTRIBUTE, servletName);
	}

	public static void exposeForwardRequestAttributes(HttpServletRequest request) {
		exposeRequestAttributeIfNotPresent(request, FORWARD_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
		exposeRequestAttributeIfNotPresent(request, FORWARD_CONTEXT_PATH_ATTRIBUTE, request.getContextPath());
		exposeRequestAttributeIfNotPresent(request, FORWARD_SERVLET_PATH_ATTRIBUTE, request.getServletPath());
		exposeRequestAttributeIfNotPresent(request, FORWARD_PATH_INFO_ATTRIBUTE, request.getPathInfo());
		exposeRequestAttributeIfNotPresent(request, FORWARD_QUERY_STRING_ATTRIBUTE, request.getQueryString());
	}
	
	private static void exposeRequestAttributeIfNotPresent(ServletRequest request, String name, Object value) {
		if (request.getAttribute(name) == null) {
			request.setAttribute(name, value);
		}
	}

}
