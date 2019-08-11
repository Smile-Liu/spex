package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {

	/**
	 * @param handler
	 * @return 该HandlerAdapter是否支持给定的Handler
	 */
	boolean supports(Object handler);
	
	
	ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
	
	
	/**
	 * 返回-1表示不支持该Handler
	 * @param request
	 * @param handler
	 * @return 上次更新时间
	 */
	long getLastModified(HttpServletRequest request, Object handler);
}
