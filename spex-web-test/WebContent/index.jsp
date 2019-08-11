<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.spex.controller.LoginController"%>
<%@ page import="org.spex.web.context.support.WebApplicationContextUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>首页</title>
</head>
<body>
<%
	LoginController login = 
		(LoginController) WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext()).getBean("login");
	out.println(login.index());
	
	String servletPath = request.getServletPath();
	out.println("<br/> servletPath:" + servletPath);
	
	String uri = request.getRequestURI();
	out.println("<br/> uri:" + uri);
	
	String contextPath = request.getContextPath();
	out.println("<br/> contextPath:" + contextPath);
	
	String pathWithinApp = uri.substring(contextPath.length());
	out.println("<br/> pathWithinApp:" + pathWithinApp);
	
	//out.println("pathWithinServletMapping:" + pathWithinApp.substring(servletPath.length()));
%>
</body>
</html>