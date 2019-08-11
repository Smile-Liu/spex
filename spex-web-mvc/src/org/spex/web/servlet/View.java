package org.spex.web.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {

	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";
	
	String getContentType();
	
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
