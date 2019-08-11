package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ThemeResolver {
	
	String resolveThemeName(HttpServletRequest request);
	
	void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName);

}
