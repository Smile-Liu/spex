package org.spex.web.servlet.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.util.StringUtils;
import org.spex.web.util.WebUtils;

public class InternalResourceView extends AbstractUrlBasedView {
	
	private boolean alwaysInclude = false;
	
	private volatile Boolean exposeForwardAttributes;
	
	private boolean exposeContextBeansAsAttributes = false;
	
	private Set<String> exposedContextBeanNames;
	
	private boolean preventDispatchLoop = false;
	
	public InternalResourceView() {}
	
	public InternalResourceView(String url) {
		super(url);
	}

	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	public void setExposeForwardAttributes(Boolean exposeForwardAttributes) {
		this.exposeForwardAttributes = exposeForwardAttributes;
	}

	public void setExposeContextBeansAsAttributes(
			boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
	}

	public void setExposedContextBeanNames(String[] exposedContextBeanNames) {
		this.exposedContextBeanNames = new HashSet<String>(Arrays.asList(exposedContextBeanNames));
	}

	public void setPreventDispatchLoop(boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		HttpServletRequest requestToExpose = getRequestToExpose(request);
		
		exposeModelAsRequestAttributes(model, requestToExpose);
		
		String dispatcherPath = prepareForRendering(requestToExpose, response);
		
		RequestDispatcher rd = requestToExpose.getRequestDispatcher(dispatcherPath);
		if (rd == null) {
			throw new ServletException("获取不到[" + getUrl() + "]的RequestDispatcher，请确认路径是否存在");
		}
		
		if (useInclude(requestToExpose, response)) {
			response.setContentType(getContentType());
			rd.include(requestToExpose, response);
		} else {
			exposeForwardRequestAttributes(requestToExpose);
			rd.forward(requestToExpose, response);
		}
	}

	protected HttpServletRequest getRequestToExpose(HttpServletRequest originalRequest) {
		return originalRequest;
	}
	
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = getUrl();
		if (this.preventDispatchLoop) {
			String uri = request.getRequestURI();
			if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {
				throw new ServletException("循环的路径引用，视图：" + path + ", 请求路径：" + uri);
			}
		}
		return path;
	}
	
	protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
		return this.alwaysInclude || WebUtils.isIncludeRequest(request) || response.isCommitted();
	}
	
	protected void exposeForwardRequestAttributes(HttpServletRequest request) {
		if (this.exposeForwardAttributes != null && this.exposeForwardAttributes) {
			WebUtils.exposeForwardRequestAttributes(request);
		}
	}
}
