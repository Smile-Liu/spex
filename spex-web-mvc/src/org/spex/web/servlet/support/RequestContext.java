package org.spex.web.servlet.support;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

	public RequestContext(HttpServletRequest request) {
		
	}
	
	public RequestContext(HttpServletRequest request, HttpServletResponse response, 
			ServletContext servletContext, Map<String, Object> model) {
		
	}
	
	
}
