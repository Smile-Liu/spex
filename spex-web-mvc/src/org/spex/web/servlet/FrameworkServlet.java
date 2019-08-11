package org.spex.web.servlet;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.beans.BeanUtils;
import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationContextException;
import org.spex.context.ApplicationListener;
import org.spex.context.event.ContextRefreshedEvent;
import org.spex.context.event.SourceFilteringListener;
import org.spex.util.LoggerUtil;
import org.spex.web.context.ConfigurableWebApplicationContext;
import org.spex.web.context.WebApplicationContext;
import org.spex.web.context.support.ServletRequestHandledEvent;
import org.spex.web.context.support.WebApplicationContextUtils;
import org.spex.web.context.support.XmlWebApplicationContext;
import org.spex.web.util.WebUtils;

public abstract class FrameworkServlet extends HttpServletBean {

	private static final long serialVersionUID = -8558853823390163335L;

	private static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;
	
	private static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";
	private static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";
	
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

	private WebApplicationContext webApplicationContext;
	
	private String namespace;
	
	private String contextConfigLocation;
	
	/** 标记是否调用OnRefresh */
	private boolean refreshEventReceived = false;
	
	/** 是否应该转发Options请求 */
	private boolean dispatchOptionsRequest = false;
	
	/** 是否应该转发Trace请求 */
	private boolean dispatchTraceRequest = false;
	
	/** 是否应该在请求结束时发布ServletRequestHandlerEvent */
	private boolean publishEvents = true;
	
	@Override
	protected void initServletBean() throws ServletException {
		LoggerUtil.info("开始初始化FrameworkServlet：" + getServletName());
		
		long startTime = System.currentTimeMillis();
		
		this.webApplicationContext = initWebApplicationContext();
		initFrameworkServlet(); // 空实现
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		LoggerUtil.info("FrameworkServlet：" + getServletName() + " 初始化完成，耗时：" + elapsedTime + "ms");
	}

	protected WebApplicationContext initWebApplicationContext() {
		WebApplicationContext wac = findWebApplicationContext();
		if (wac == null) {
			wac = createWebApplicationContext();
		}
		
		if (!this.refreshEventReceived) {
			onRefresh(wac);
		}
		
		if (this.publishEvents) {
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
		}
		return wac;
	}
	
	protected WebApplicationContext createWebApplicationContext() {
		Class<?> contextClass = getContextClass();
		LoggerUtil.info("Servlet with name '" + getServletName() +
				"' will try to create custom WebApplicationContext context of class '" +
				contextClass.getName() + "'");
		
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("自定义的WebApplicationContext[" + contextClass.getName() + "]" +
					"必须是ConfigurableWebApplicationContext类型");
		}
		
		ConfigurableWebApplicationContext wac = 
			(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		
		// 设置ID
		ServletContext sc = getServletContext();
		if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
			String contextName = sc.getServletContextName();
			if (contextName == null) {
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
			} else {
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + contextName + "." + getServletName());
			}
		} else {
			wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + sc.getContextPath() + "/" + getServletName());
		}
		
		wac.setServletContext(getServletContext());
		wac.setServletConfig(getServletConfig());
		wac.setNamespace(getNamespace());
		wac.setConfigLocation(getContextConfigLocation());
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));
		
		postProcessWebApplicationContext(wac);
		wac.refresh();
		
		return wac;
	}
	
	protected WebApplicationContext findWebApplicationContext() {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return wac;
	}
	
	protected void onRefresh(ApplicationContext context) {
		// 留给子类实现
	}
	
	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {}
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		onRefresh(event.getApplicationContext());
	}
	
	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}
	// ================== Request & Response =====================
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		processRequest(request, response);
	}
	
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	
		processRequest(request, response);
	}
	
	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	
		processRequest(request, response);
	}
	
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	
		processRequest(request, response);
	}
	
	protected final void doOptions(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	
		super.doOptions(request, response);
		if (this.dispatchOptionsRequest) {
			processRequest(request, response);
		}
	}
	
	protected final void doTrace(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	
		super.doTrace(request, response);
		if (this.dispatchTraceRequest) {
			processRequest(request, response);
		}
	}
	
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;
		
		try {
			doService(request, response);
		} catch (ServletException e) {
			failureCause = e;
			throw e;
		} catch (IOException e) {
			failureCause = e;
			throw e;
		} catch (Throwable e) {
			failureCause = e;
			throw new ServletException("Request process failed", e);
		} finally {
			
			if (failureCause != null) {
				LoggerUtil.error("没有完成请求", failureCause);
			} else {
				LoggerUtil.info("请求成功");
			}
			
			if (this.publishEvents) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				this.webApplicationContext.publishEvent(new ServletRequestHandledEvent(this,
						WebUtils.getSessionId(request), getUserNameForRequest(request), elapsedTime, failureCause,
						request.getRequestURI(), request.getRemoteAddr(), request.getMethod(), getServletConfig().getServletName()));
			}
		}
	}
	
	protected String getUserNameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return userPrincipal != null ? userPrincipal.getName() : null;
	}
	
	@Override
	public void destroy() {
		getServletContext().log("销毁FrameworkServlet '" + getServletName() + "'");
		if (this.webApplicationContext instanceof ConfigurableWebApplicationContext) {
			((ConfigurableWebApplicationContext) this.webApplicationContext).close();
		}
	}
	
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
		throws Exception;
	
	
	public final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}
	
	public Class<?> getContextClass() {
		return contextClass;
	}

	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	public String getNamespace() {
		return namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}

	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}

	protected void initFrameworkServlet() {}
	
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
		
	}
}
