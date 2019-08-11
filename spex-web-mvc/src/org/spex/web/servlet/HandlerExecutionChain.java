package org.spex.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spex.core.io.support.CollectionUtils;

public class HandlerExecutionChain {

	private final Object handler;
	
	private HandlerInterceptor[] interceptors;
	
	// Ö÷ÒªµÄ
	private List<HandlerInterceptor> interceptorList;
	
	public HandlerExecutionChain(Object handler) {
		this(handler, null);
	}
	
	public HandlerExecutionChain(Object handler, HandlerInterceptor[] interceptors) {
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
			
			this.handler = originalChain.getHandler();
			this.interceptorList = new ArrayList<HandlerInterceptor>();
			
			CollectionUtils.mergeArrayIntoCollection(originalChain.getInterceptors(), interceptorList);
			CollectionUtils.mergeArrayIntoCollection(interceptors, interceptorList);
		} else {
			this.handler = handler;
			this.interceptors = interceptors;
		}
	}

	public HandlerInterceptor[] getInterceptors() {
		return interceptors;
	}

	public Object getHandler() {
		return handler;
	}
	
	public void addInterceptor(HandlerInterceptor interceptor) {
		initInterceptorList();
		this.interceptorList.add(interceptor);
	}
	
	public void addInterceptors(HandlerInterceptor[] interceptors) {
		if (interceptors != null) {
			initInterceptorList();
			this.interceptorList.addAll(Arrays.asList(interceptors));
		}
	}
	
	public void initInterceptorList() {
		if (this.interceptorList == null) {
			this.interceptorList = new ArrayList<HandlerInterceptor>();
		}
		if (this.interceptors != null) {
			this.interceptorList.addAll(Arrays.asList(this.interceptors));
			this.interceptors = null;
		}
	}
}
