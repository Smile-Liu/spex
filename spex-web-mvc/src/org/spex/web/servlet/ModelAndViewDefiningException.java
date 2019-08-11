package org.spex.web.servlet;

import javax.servlet.ServletException;

public class ModelAndViewDefiningException extends ServletException {

	private static final long serialVersionUID = 5225830977785040468L;

	private ModelAndView modelAndView;
	
	public ModelAndViewDefiningException(ModelAndView modelAndView) {
		this.modelAndView = modelAndView;
	}
	
	public ModelAndView getModelAndView() {
		return this.modelAndView;
	}
}
