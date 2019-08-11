package org.spex.web.servlet.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.spex.context.ApplicationContext;
import org.spex.core.Ordered;
import org.spex.web.context.support.WebApplicationObjectSupport;
import org.spex.web.servlet.HandlerExecutionChain;
import org.spex.web.servlet.HandlerInterceptor;
import org.spex.web.servlet.HandlerMapping;

public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
		implements HandlerMapping, Ordered {

	private int order = Integer.MAX_VALUE;
	
	private Object defaultHandler;
	
	private HandlerInterceptor[] adpatedInterceptors;
	
	private final List<Object> interceptors = new ArrayList<Object>();
	
	public final void setOrder(int order) {
		this.order = order;
	}

	@Override
	public final int getOrder() {
		return this.order;
	}

	public Object getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	public void setInterceptors(Object[] interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}

	public final HandlerInterceptor[] getAdpatedInterceptors() {
		return adpatedInterceptors;
	}

	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			this.adpatedInterceptors = new HandlerInterceptor[this.interceptors.size()];
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalStateException("Interceptor集合的第" + i + "项是空的");
				}
				this.adpatedInterceptors[i] = adaptInterceptor(interceptor);
			}
		}
	}
	
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
//		if (interceptor instanceof WebRequestInterceptor) {
//			
//		}
		throw new IllegalArgumentException("不支持的Interceptor");
	}
	
	@Override
	protected void initApplicationContext(ApplicationContext context) {
		extendInterceptors(this.interceptors);
		initInterceptors();
	}
	
	protected void extendInterceptors(List<Object> interceptors) {}
	
	protected abstract Object getHandlerInternal(HttpServletRequest request);
	
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain chain = (HandlerExecutionChain) handler;
			chain.addInterceptors(getAdpatedInterceptors());
			return chain;
		} else {
			return new HandlerExecutionChain(handler, getAdpatedInterceptors());
		}
	}
	
	@Override
	public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {

		Object handler = getHandlerInternal(request);
		if (handler == null) {
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		return getHandlerExecutionChain(handler, request);
	}

}
