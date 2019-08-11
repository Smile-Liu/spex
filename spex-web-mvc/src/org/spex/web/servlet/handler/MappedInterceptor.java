package org.spex.web.servlet.handler;

import org.spex.web.servlet.HandlerInterceptor;

public final class MappedInterceptor {

	private final String[] pathPatterns;
	
	private final HandlerInterceptor interceptor;
	
	public MappedInterceptor(String[] pathPatterns, HandlerInterceptor interceptor) {
		this.pathPatterns = pathPatterns;
		this.interceptor = interceptor;
	}

	public String[] getPathPatterns() {
		return pathPatterns;
	}

	public HandlerInterceptor getInterceptor() {
		return interceptor;
	}
	
}
