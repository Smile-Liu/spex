package org.spex.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.web.servlet.ModelAndView;

public interface Controller {

	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response);
}
