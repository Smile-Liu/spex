package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {

	/**
	 * @param handler
	 * @return ��HandlerAdapter�Ƿ�֧�ָ�����Handler
	 */
	boolean supports(Object handler);
	
	
	ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
	
	
	/**
	 * ����-1��ʾ��֧�ָ�Handler
	 * @param request
	 * @param handler
	 * @return �ϴθ���ʱ��
	 */
	long getLastModified(HttpServletRequest request, Object handler);
}
