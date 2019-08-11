package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerExceptionResolver {

	ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, 
			Object handler, Exception ex);
}
