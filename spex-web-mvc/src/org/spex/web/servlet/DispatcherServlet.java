package org.spex.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.beans.BeansException;
import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.beans.factory.NoSuchBeanDefinitionException;
import org.spex.context.ApplicationContext;
import org.spex.core.OrderComparator;
import org.spex.ui.context.ThemeSource;
import org.spex.util.ClassUtils;
import org.spex.util.LoggerUtil;
import org.spex.util.StringUtils;
import org.spex.web.multipart.MultipartException;
import org.spex.web.multipart.MultipartHttpServletRequest;
import org.spex.web.multipart.MultipartResolver;
import org.spex.web.util.WebUtils;

public class DispatcherServlet extends FrameworkServlet {

	private static final long serialVersionUID = -1559376919889019403L;

	private static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";
	private static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";
	private static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";
	private static final String HANDLER_EXECUTION_CHAIN_ATTRIBUTE = DispatcherServlet.class.getName() + ".HANDLER";
	
	private static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";
	private static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";
	private static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";
	private static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";
	private static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerAdapter";
	private static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";
	
	private boolean cleanupAfterInclude = true;
	
	/** 探查所有的HandlerMapping */
	private boolean detectAllHandlerMapping = true;
	
	private boolean detectAllHandlerAdapter = true;
	
	private boolean detectAllHandlerExceptionResolvers = true;
	
	private boolean detectAllViewResolvers = true;
	
	private ThemeResolver themeResolver;
	
	private MultipartResolver multipartResolver;
	
	private List<HandlerMapping> handlerMappings;
	
	private List<HandlerAdapter> handlerAdapters;
	
	private RequestToViewNameTranslator viewNameTranslator;
	
	private List<HandlerExceptionResolver> handlerExceptionResolvers;
	
	private List<ViewResolver> viewResolvers;
	
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";
	private static final Properties defaultStrategies;
	
	static {
		try {
			InputStream is = DispatcherServlet.class.getClassLoader().getResourceAsStream(DEFAULT_STRATEGIES_PATH);
			defaultStrategies = new Properties();
			defaultStrategies.load(is);
		} catch (IOException e) {
			throw new IllegalStateException("加载'DispatcherServlet.properties'失败：" + e.getMessage());
		}
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String requestUri = request.getRequestURI();
		LoggerUtil.info("DispatcherServlet with name '" + getServletName() + "' processing " + request.getMethod() +
				" request for [" + requestUri + "]");
		
		Map<String, Object> attributesSnapshot = null;
		if (WebUtils.isIncludeRequest(request)) {
			LoggerUtil.info("Taking snapshot of request attributes before include");
			attributesSnapshot = new HashMap<String, Object>();
			
			Enumeration<?> attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith("org.spex.web.servlet")) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}
		
		// 给request添加几个属性
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());
		
		// 分发
		try {
			doDispatch(request, response);
		} finally {
			if (attributesSnapshot != null) {
				// 重新存储Request属性
				restorAttributesAfterInclude(request, attributesSnapshot);
			}
		}
	}

	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processRequest = request;
		HandlerExecutionChain mappedHandler = null;
		int interceptorIndex = -1;
		
		try {
			ModelAndView mv;
			boolean errorView = false;
			
			try {
				// 如果是Multipart，则转换为MultipartHttpServletRequest
				processRequest = checkMultipart(request);
				
				// 找到HandlerMapping的处理类（BeanNameUrlHandlerMapping）
				// 匹配Request中的请求路径，找到对应的处理逻辑bean（也就是Controller），当做handler，存入Chain
				mappedHandler = getHandler(processRequest, false);
				if (mappedHandler == null || mappedHandler.getHandler() == null) {
					noHandlerFount(request, response);
					return ;
				}
				
				// 执行 Handler 中设置的拦截器Interceptor
				HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
				if (interceptors != null) {
					for (int i = 0; i < interceptors.length; i++) {
						HandlerInterceptor interceptor = interceptors[i];
						
						// preHandler为false，则中断流程
						if (!interceptor.preHandler(processRequest, response, mappedHandler.getHandler())) {
							triggerAfterCompletion(request, response, mappedHandler, interceptorIndex, null);
							return;
						}
						
						interceptorIndex = i;
					}
				}
				
				// 获取匹配的HandlerAdapter
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
				mv = ha.handler(processRequest, response, mappedHandler.getHandler());
				
				// view name转换
				if (mv != null && !mv.hasView()) {
					mv.setViewName(getDefaultViewName(request));
				}
				
				// 执行 Handler 中设置的拦截器Interceptor
				if (interceptors != null) {
					for (int i = 0; i < interceptors.length; i++) {
						HandlerInterceptor interceptor = interceptors[i];
						interceptor.postHandler(processRequest, response, mappedHandler.getHandler(), mv);
					}
				}
			} catch (ModelAndViewDefiningException e) {
				mv = e.getModelAndView();
			} catch (Exception e) {
				Object handler = mappedHandler != null ? mappedHandler.getHandler() : null;
				mv = processHandlerException(processRequest, response, handler, e);
				errorView = (mv != null);
			}
			
			if (mv != null && !mv.wasCleared()) {
				render(mv, processRequest, response);
			}
			
			// 执行拦截器的AfterCompletion方法
			triggerAfterCompletion(processRequest, response, mappedHandler, interceptorIndex, null);
		} catch (Exception e) {
			// 抛异常后，继续走完拦截器的AfterCompletion方法
			triggerAfterCompletion(processRequest, response, mappedHandler, interceptorIndex, e);
			throw e;
		} catch (Error err) {
			// 发生错误时，抛出ServletException，并走完拦截器的AfterCompletion方法
			ServletException ex = new ServletException("处理请求失败", err);
			
			triggerAfterCompletion(processRequest, response, mappedHandler, interceptorIndex, ex);
			throw ex;
		} finally {
			// 当是文件上传请求时，清理
			if (processRequest != request) {
				clearupMultipart(processRequest);
			}
		}
	}
	
	protected void clearupMultipart(HttpServletRequest request) {
		if (request instanceof MultipartHttpServletRequest) {
			this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) request);
		}
	}
	
	protected HandlerExecutionChain getHandler(HttpServletRequest request, boolean cache) throws Exception {
		HandlerExecutionChain handler = (HandlerExecutionChain) request.getAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE);
		if (handler != null) {
			if (!cache) {
				request.removeAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE);
			}
			return handler;
		}
		
		for (HandlerMapping hm : this.handlerMappings) {
			handler = hm.getHandler(request);
			if (handler != null) {
				if (cache) {
					request.setAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE, handler);
				}
				return handler;
			}
		}
		return null;
	}
	
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		for (HandlerAdapter ha : this.handlerAdapters) {
			LoggerUtil.info("测试 handler adapter [" + ha + "] 是否支持");
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("没有找到Handler adapter");
	}
	
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (request instanceof MultipartHttpServletRequest) {
				LoggerUtil.info("当前请求已经是MultipartHttpServletRequest了，无需转换。可能是在web.xml中配置了MultipartFilter");
			} else {
				return this.multipartResolver.resolveMultipart(request);
			}
		}
		return request;
	}
	
	protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
		List<T> strategies = getDefaultStrategies(context, strategyInterface);
		if (strategies.size() != 1) {
			throw new BeansException("DispatcherServlet仅需要一个["+ strategyInterface.getName() +"]实现类");
		}
		return strategies.get(0);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
		String key = strategyInterface.getName();
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<T>(classNames.length);
			
			for (String className : classNames) {
				try {
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					
					// 创建Bean的过程中，会执行setApplicationContext（是Aware后置处理器）
					// 此时，会遍历所有以“/”开头的beanName，作为path，对应的bean就是HandlerMapping
					Object strategy = context.getListableBeanFactory().createBean(clazz);
					strategies.add((T) strategy);
				} catch (ClassNotFoundException e) {
					throw new BeansException("未找到DispatcherServlet中[" + key + "]的默认指定实现类[" + className + "]", e);
				}
			}
			return strategies;
		}
		return new LinkedList<T>();
	}
	
	protected void noHandlerFount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LoggerUtil.info(request.getRequestURI() + " 请求未找到对应的Handler Mapping，Servlet：" + getServletName());
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	protected String getDefaultViewName(HttpServletRequest request) throws Exception {
		return this.viewNameTranslator.getViewName(request);
	}
	
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) throws Exception {
		
		ModelAndView exMv = null;
		for (HandlerExceptionResolver her : this.handlerExceptionResolvers) {
			exMv = her.resolveException(request, response, handler, ex);
			if (exMv != null) {
				break;
			}
		}
		if (exMv != null) {
			if (exMv.isEmpty()) {
				return null;
			}
			WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
			return exMv;
		}
		throw ex;
	}
	
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Locale locale = Locale.CHINA;
		response.setLocale(locale);
		
		View view;
		if (mv.isReference()) {
			view = resolveViewName(mv.getViewName(), mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("解析Servlet[" + getServletName() + "]的View[" + mv.getViewName() + "]失败");
			}
		} else {
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView[" + mv + "]既不包含视图名称，又不是视图对象");
			}
		}
		view.render(mv.getModelInternal(), request, response);
	}
	
	protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale, 
			HttpServletRequest request) throws Exception {
		
		for (ViewResolver viewResolver : this.viewResolvers) {
			View view = viewResolver.resolveViewName(viewName, locale);
			if (view != null) {
				return view;
			}
		}
		 return null;
	}
	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}

	public final ThemeSource getThemeSource() {
		if (getWebApplicationContext() instanceof ThemeSource) {
			return (ThemeSource) getWebApplicationContext();
		} else {
			return null;
		}
	}
	
	public MultipartResolver getMultipartResolver() {
		return multipartResolver;
	}

	public void setDetectAllHandlerMapping(boolean detectAllHandlerMapping) {
		this.detectAllHandlerMapping = detectAllHandlerMapping;
	}

	public void setDetectAllHandlerAdapter(boolean detectAllHandlerAdapter) {
		this.detectAllHandlerAdapter = detectAllHandlerAdapter;
	}

	protected void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, int interceptorIndex, Exception ex) {
		
		if (mappedHandler != null) {
			HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
			if (interceptors != null) {
				// 只有执行
				for (int i = interceptorIndex; i >= 0; i--) {
					HandlerInterceptor interceptor = interceptors[i];
					try {
						interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
					} catch (Exception e) {
						LoggerUtil.error("HandlerInterceptor.afterCompletion抛出异常", e);
					}
				}
			}
		}
	}
	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			
			LoggerUtil.info("Using ThemeResolver [" + this.themeResolver + "]");
		} catch (NoSuchBeanDefinitionException e) {
			this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
			
			LoggerUtil.info("没有指定的类，Using default ThemeResolver [" + this.themeResolver + "]");
		}
	}
	
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			
			LoggerUtil.info("Using MultipartResolver [" + this.multipartResolver + "]");
		} catch (NoSuchBeanDefinitionException e) {
			this.multipartResolver = null;
			
			LoggerUtil.info("没有指定的类，Using default MultipartResolver [" + this.multipartResolver + "]");
		}
	}

	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;
		
		if (this.detectAllHandlerMapping) {
			Map<String, HandlerMapping> matchingBeans =
				BeanFactoryUtils.beansForTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (matchingBeans != null) {
				this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
				OrderComparator.sort(handlerMappings);
			}
		} else {
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			} catch (NoSuchBeanDefinitionException e) {
				// 
			}
		}
		
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			LoggerUtil.info("没有指定的类，Using default HandlerMapping [" + this.handlerMappings + "]");
		}
	}
	
	private void initHandlerAdapter(ApplicationContext context) {
		this.handlerAdapters = null;
		
		if (this.detectAllHandlerAdapter) {
			Map<String, HandlerAdapter> matchingBeans = 
				BeanFactoryUtils.beansForTypeIncludingAncestors(getWebApplicationContext(), HandlerAdapter.class, true, true);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
				OrderComparator.sort(this.handlerAdapters);
			}
		} else {
			try {
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			} catch (NoSuchBeanDefinitionException e) {
				// handle exception
			}
		}
		
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			LoggerUtil.info("没有指定的类，Using default HandlerAdapter [" + this.handlerAdapters + "]");
		}
	}
	
	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;
		
		if (this.detectAllHandlerExceptionResolvers) {
			Map<String, HandlerExceptionResolver> matchingBeans =
				BeanFactoryUtils.beansForTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (matchingBeans != null) {
				this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
				OrderComparator.sort(handlerExceptionResolvers);
			}
		} else {
			try {
				HandlerExceptionResolver her = context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			} catch (NoSuchBeanDefinitionException e) {
				// 
			}
		}
		
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			LoggerUtil.info("没有指定的类，Using default HandlerExceptionResolver [" + this.handlerExceptionResolvers + "]");
		}
	}
	
	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator = context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
			
			LoggerUtil.info("Using MultipartResolver [" + this.multipartResolver + "]");
		} catch (NoSuchBeanDefinitionException e) {
			this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
			
			LoggerUtil.info("没有指定的类，Using default RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
		}
	}

	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;
		
		if (this.detectAllViewResolvers) {
			Map<String, ViewResolver> matchingBeans =
				BeanFactoryUtils.beansForTypeIncludingAncestors(context, ViewResolver.class, true, false);
			if (matchingBeans != null) {
				this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
				OrderComparator.sort(handlerExceptionResolvers);
			}
		} else {
			try {
				ViewResolver viewResolver = context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(viewResolver);
			} catch (NoSuchBeanDefinitionException e) {
				// 
			}
		}
		
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
			LoggerUtil.info("没有指定的类，Using default ViewResolver [" + this.viewResolvers + "]");
		}
	}
	
	private void restorAttributesAfterInclude(HttpServletRequest request, Map<String, Object> attributesSnapshot) {
		LoggerUtil.info("Restoring snapshot of request attributes after include");
		
		// 找到处理后的属性
		Set<String> attrsToCheck = new HashSet<String>();
		Enumeration<?> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith("org.spex.web.servlet")) {
				attrsToCheck.add(attrName);
			}
		}
		
		// 处理后属性列表中，
		// 如果在处理前的属性中不存在，则要删除
		// 如果处理前和处理后的属性值不一致，则保留处理前的属性值
		for (String attrName : attrsToCheck) {
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue == null) {
				request.removeAttribute(attrName);
			}
			else if (attrValue != request.getAttribute(attrName)) {
				request.setAttribute(attrName, attrValue);
			}
		}
	}
}
