package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;

public interface RequestToViewNameTranslator {

	String getViewName(HttpServletRequest request) throws Exception;
}
