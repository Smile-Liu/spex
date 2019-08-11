package org.spex.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.spex.beans.BeanUtils;
import org.spex.core.Ordered;
import org.spex.core.io.support.CollectionUtils;
import org.spex.util.PatternMatchUtils;
import org.spex.web.servlet.View;

public class UrlBasedViewResolver extends AbstractCachingViewResolver implements Ordered {

	public static final String REDIRECT_URI_PREFIX = "redirect:";
	
	public static final String FORWARD_URI_PREFIX = "forward:";
	
	private Class<?> viewClass;
	
	private String prefix = "";
	
	private String suffix = "";
	
	private String[] viewNames = null;
	
	private String contentType;
	
	private boolean redirectContextRelative = true;
	
	private boolean redirectHttp10Compatible = true;
	
	private String requestContextAttribute;
	
	private int order = Integer.MAX_VALUE;
	
	private final Map<String, Object> staticAttributes = new HashMap<String, Object>();
	
	
	public Class<?> getViewClass() {
		return viewClass;
	}

	public void setViewClass(Class<?> viewClass) {
		if (viewClass == null || !requiredViewClass().isAssignableFrom(viewClass)) {
			throw new IllegalArgumentException("给定的视图[" + (viewClass != null ? viewClass.getName() : null) + "]" +
					"不是AbstractUrlBasedView");
		}
		this.viewClass = viewClass;
	}

	protected Class<?> requiredViewClass() {
		return AbstractUrlBasedView.class;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix != null ? prefix : "";
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix != null ? suffix : "";
	}

	public String[] getViewNames() {
		return viewNames;
	}

	public void setViewNames(String[] viewNames) {
		this.viewNames = viewNames;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setAttributes(Properties props) {
		CollectionUtils.mergePropertiesIntoMap(props, this.staticAttributes);
	}

	public void setAttributesMap(Map<String, ?> attributes) {
		if (attributes != null) {
			this.staticAttributes.putAll(attributes);
		}
	}
	
	public Map<String, Object> getAttributesMap() {
		return staticAttributes;
	}

	public boolean isRedirectContextRelative() {
		return redirectContextRelative;
	}

	public void setRedirectContextRelative(boolean redirectContextRelative) {
		this.redirectContextRelative = redirectContextRelative;
	}

	public boolean isRedirectHttp10Compatible() {
		return redirectHttp10Compatible;
	}

	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}

	public String getRequestContextAttribute() {
		return requestContextAttribute;
	}

	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName;
	}

	@Override
	protected View createView(String viewName, Locale locale) throws Exception {
		if (!canHandle(viewName, locale)) {
			return null;
		}
		if (viewName.startsWith(REDIRECT_URI_PREFIX)) {
			String redirectUrl = viewName.substring(REDIRECT_URI_PREFIX.length());
			return new RedirectView(redirectUrl, isRedirectContextRelative(), isRedirectHttp10Compatible());
		}
		if (viewName.startsWith(FORWARD_URI_PREFIX)) {
			String forwardUrl = viewName.substring(FORWARD_URI_PREFIX.length());
			return new InternalResourceView(forwardUrl);
		}
		return super.createView(viewName, locale);
	}
	
	protected boolean canHandle(String viewName, Locale locale) {
		String[] viewNames = getViewNames();
		return viewNames == null || PatternMatchUtils.simpleMatch(viewNames, viewName);
	}
	
	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		AbstractUrlBasedView view = buildView(viewName);
		View result = (View) getApplicationContext().getListableBeanFactory().initializeBean(view, viewName);
		return result;
	}
	
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		AbstractUrlBasedView view = (AbstractUrlBasedView) BeanUtils.instantiateClass(getViewClass());
		view.setUrl(getPrefix() + viewName + getSuffix());
		String contentType = getContentType();
		if (contentType != null) {
			view.setContentType(contentType);
		}
		view.setRequestContextAttribute(getRequestContextAttribute());
		view.setAttributesMap(getAttributesMap());
		return view;
	}
}
