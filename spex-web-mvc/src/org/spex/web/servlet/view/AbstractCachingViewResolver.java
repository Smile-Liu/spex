package org.spex.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.spex.web.context.support.WebApplicationObjectSupport;
import org.spex.web.servlet.View;
import org.spex.web.servlet.ViewResolver;

public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	private boolean cache = true;
	
	private final Map<Object, View> viewCache = new HashMap<Object, View>();
	
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		if (!isCache()) {
			return createView(viewName, locale);
		} else {
			Object cachedKey = getCacheKey(viewName, locale);
			synchronized(this.viewCache) {
				View view = this.viewCache.get(cachedKey);
				if (view == null) {
					view = createView(viewName, locale);
					this.viewCache.put(cachedKey, view);
				}
				return view;
			}
		}
	}

	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}
	
	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public void clearCache() {
		synchronized (this.viewCache) {
			this.viewCache.clear();
		}
	}
	
	protected View createView(String viewName, Locale locale) throws Exception {
		return loadView(viewName, locale);
	}
	
	protected abstract View loadView(String viewName, Locale locale) throws Exception;
}
