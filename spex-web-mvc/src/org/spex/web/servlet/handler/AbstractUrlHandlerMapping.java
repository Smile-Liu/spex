package org.spex.web.servlet.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.util.AntPathMatcher;
import org.spex.util.LoggerUtil;
import org.spex.util.PathMatcher;
import org.spex.web.servlet.HandlerExecutionChain;
import org.spex.web.servlet.HandlerMapping;
import org.spex.web.util.UrlPathHelper;

public class AbstractUrlHandlerMapping extends AbstractHandlerMapping {

	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();
	
	private Object rootHandler;
	
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		if (urlPathHelper != null) {
			this.urlPathHelper = urlPathHelper;
		}
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	public Object getRootHandler() {
		return rootHandler;
	}

	public void setRootHandler(Object rootHandler) {
		this.rootHandler = rootHandler;
	}

	protected void initInterceptors() {
		super.initInterceptors();
		Map<String, MappedInterceptor> mappedInterceptors = 
			BeanFactoryUtils.beansForTypeIncludingAncestors(getApplicationContext(), MappedInterceptor.class, true, false);
		if (!mappedInterceptors.isEmpty()) {
			
		}
	}
	
	protected void registerHandler(String[] urlPaths, String beanName) {
		for (String urlPath : urlPaths) {
			registerHandler(urlPath, beanName);
		}
	}
	
	protected void registerHandler(String urlPath, Object handler) {
		Object resolvedHandler = handler;
		
		if (handler instanceof String) {
			String handlerName = (String) handler;
			resolvedHandler = getApplicationContext().getBean(handlerName);
		}
		
		Object mappedHandler = this.handlerMap.get(urlPath);
		if (mappedHandler != null) {
			if (mappedHandler != resolvedHandler) {
				throw new IllegalStateException("请求URL[" + urlPath + "]已经有Handler映射了，不能再映射'" + handler + "'");
			}
		} else {
			if ("/".equals(urlPath)) {
				setRootHandler(resolvedHandler);
			} else if ("/*".equals(urlPath)) {
				setDefaultHandler(resolvedHandler);
			} else {
				this.handlerMap.put(urlPath, resolvedHandler);
			}
		}
	}
	
	/** 
	 * 在AbstractHandlerMapping.getHandler方法中执行的
	 * @see org.spex.web.servlet.handler.AbstractHandlerMapping#getHandlerInternal(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object getHandlerInternal(HttpServletRequest request) {
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		Object handler = lookupHandler(lookupPath, request);
		
		if (handler == null) {
			// 找不到，则匹配默认值
			Object rawHandler = null;
			if ("/".equals(lookupPath)) {
				rawHandler = getRootHandler();
			}
			if (rawHandler == null) {
				rawHandler = getDefaultHandler();
			}
			if (rawHandler != null) {
				if (rawHandler instanceof String) {
					String handlerName = (String) rawHandler;
					rawHandler = getApplicationContext().getBean(handlerName);
				}
				validateHandler(rawHandler, request);
				handler = buildPathExposingHandler(rawHandler, lookupPath, lookupPath, null);
			}
		}
		if (handler != null) {
			LoggerUtil.info(lookupPath + " 请求找到对应的Handler Mapping：" + handler);
		} else {
			LoggerUtil.info(lookupPath + " 请求未找到Handler Mapping");
		}
		return handler;
	}

	protected Object lookupHandler(String urlPath, HttpServletRequest request) {
		// 直接匹配
		Object handler = this.handlerMap.get(urlPath);
		if (handler != null) {
			if (handler instanceof String) {
				String handlerName = (String) handler;
				handler = getApplicationContext().getBean(handlerName);
			}
			validateHandler(handler, request);
			return buildPathExposingHandler(handler, urlPath, urlPath, null);
		}
		
		// 模式匹配
		List<String> matchingPatterns = new ArrayList<String>();
		for (String registeredPattern : this.handlerMap.keySet()) {
			if (getPathMatcher().match(registeredPattern, urlPath)) {
				matchingPatterns.add(registeredPattern);
			}
		}
		String bestPatternMatch = null;
		Comparator<String> patternComparator = getPathMatcher().getPatternComparator(urlPath);
		if (!matchingPatterns.isEmpty()) {
			Collections.sort(matchingPatterns, patternComparator);
			LoggerUtil.info("为请求'" + urlPath + "'找到匹配的路径：" + matchingPatterns);
			bestPatternMatch = matchingPatterns.get(0);
		}
		
		if (bestPatternMatch != null) {
			handler = this.handlerMap.get(bestPatternMatch);
			
			if (handler instanceof String) {
				String handlerName = (String) handler;
				handler = getApplicationContext().getBean(handlerName);
			}
			
			validateHandler(handler, request);
			
			String pathWithinMapping = getPathMatcher().extractPathWithinPattern(bestPatternMatch, urlPath);
			return buildPathExposingHandler(handler, bestPatternMatch, pathWithinMapping, null);
		}
		
		return null;
	}

	protected void validateHandler(Object handler, HttpServletRequest request) {}
	
	protected Object buildPathExposingHandler(Object rawHandler, String bestMatchingPattern,
			String pathWithinMapping, Map<String, String> uriTemplateVariables) {
		
		HandlerExecutionChain chain = new HandlerExecutionChain(rawHandler);
		chain.addInterceptor(new PathExposingHandlerInterceptor(bestMatchingPattern, pathWithinMapping));
		
		return chain;
	}
	
	protected void exposePathWithinMapping(String bestMatchingPattern, String pathWithinMapping, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, bestMatchingPattern);
		request.setAttribute(HandlerMapping.PATH_WITHIN_MAPPING_ATTRIBUTE, pathWithinMapping);
	}
	
	private class PathExposingHandlerInterceptor extends HandlerInterceptorAdapter {
		
		private final String bestMatchingPattern;
		
		private final String pathWithinMapping;
		
		public PathExposingHandlerInterceptor(String bestMatchingPattern, String pathWithinMapping) {
			this.bestMatchingPattern = bestMatchingPattern;
			this.pathWithinMapping = pathWithinMapping;
		}
		
		public boolean preHandler(HttpServletRequest request, HttpServletResponse response, Object handler) {
			exposePathWithinMapping(bestMatchingPattern, pathWithinMapping, request);
			return true;
		}
		
	}
}
