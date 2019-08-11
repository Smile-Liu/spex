package org.spex.web.context.request;

import java.util.Iterator;
import java.util.Map;

public interface WebRequest extends RequestAttributes {

	String getHeader(String headerName);
	
	String[] getHeaders(String headerName);
	
	Iterator<String> getHeaderNames();
	
	String getParameter(String paramName);
	
	String[] getParameters(String paramName);
	
	Iterator<String> getParameterNames();
	
	Map<String, String[]> getParameterMap();
	
	String getContextPath();
	
	boolean checkNotModified(long lastModifiedTimestamp);
}
