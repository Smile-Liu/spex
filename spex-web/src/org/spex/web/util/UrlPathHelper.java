package org.spex.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.spex.util.LoggerUtil;
import org.spex.util.StringUtils;

public class UrlPathHelper {

	private boolean alwaysUseFullPath = false;
	
	private boolean urlDecode = true;
	
	private String defaultEncoding = WebUtils.DEFAULT_CHARACTOR_ENCODING;
	
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.alwaysUseFullPath = alwaysUseFullPath;
	}

	public void setUrlDecode(boolean urlDecode) {
		this.urlDecode = urlDecode;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	private String getDefaultEncoding() {
		return defaultEncoding;
	}

	public String getLookupPathForRequest(HttpServletRequest request) {
		if (this.alwaysUseFullPath) {
			return getPathWithinApplication(request);
		}
		
		String rest = getPathWithinServletMapping(request);
		if (!"".equals(rest)) {
			return rest;
		} else {
			return getPathWithinApplication(request);
		}
	}
	
	public String getPathWithinServletMapping(HttpServletRequest request) {
		String pathWithinApp = getPathWithinApplication(request);
		String servletPath = getServletPath(request);
		if (pathWithinApp.startsWith(servletPath)) {
			return pathWithinApp.substring(servletPath.length());
		} else {
			return servletPath;
		}
	}
	
	public String getPathWithinApplication(HttpServletRequest request) {
		String contextPath = getContextPath(request);
		String requestUri = getRequestUri(request);
		
		if (StringUtils.startsWithIgnoreCase(requestUri, contextPath)) {
			String path = requestUri.substring(contextPath.length());
			return (StringUtils.hasText(path) ? path : "/");
		} else {
			return requestUri;
		}
	}
	
	public String getServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		if (servletPath.length() > 1 && servletPath.endsWith("/")) {
			servletPath = servletPath.substring(0, servletPath.length() - 1);
		}
		return servletPath;
	}
	
	public String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return decodeAndCleanUriString(request, uri);
	}
	
	public String getContextPath(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		if ("/".equals(contextPath)) {
			contextPath = "";
		}
		return decodeRequestString(request, contextPath);
	}

	@SuppressWarnings("deprecation")
	public String decodeRequestString(HttpServletRequest request, String source) {
		if (this.urlDecode) {
			String encoding = determineEncoding(request);
			try {
				return UriUtils.decode(source, encoding);
			} catch (UnsupportedEncodingException e) {
				LoggerUtil.info("½«Request×Ö·û´®'" + source + "'°´"+ encoding +"½âÂëÊ§°Ü£º" + e.getMessage());
				return URLDecoder.decode(source);
			}
		}
		return source;
	}

	public String determineEncoding(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = getDefaultEncoding();
		}
		return encoding;
	}

	private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
		uri = decodeRequestString(request, uri);
		// ·ÖºÅ
		int semicolonIndex = uri.indexOf(';');
		return semicolonIndex != -1 ? uri.substring(0, semicolonIndex) : uri;
	}
}
