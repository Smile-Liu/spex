package org.spex.web.servlet;

import java.util.Map;

import org.spex.core.io.support.CollectionUtils;
import org.spex.ui.ModelMap;

public class ModelAndView {

	private Object view;
	
	private ModelMap model;
	
	private boolean cleared = false;

	public String getViewName() {
		return this.view instanceof String ? (String) this.view : null;
	}
	
	public void setViewName(String viewName) {
		this.view = viewName;
	}
	
	public View getView() {
		return this.view instanceof View ? (View) this.view : null;
	}

	public void setView(View view) {
		this.view = view;
	}
	
	public Map<String, Object> getModelInternal() {
		return this.model;
	}
	
	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}
	
	public Map<String, Object> getModel() {
		return getModelMap();
	}

	public boolean hasView() {
		return this.view != null;
	}
	
	public boolean isReference() {
		return this.view instanceof String;
	}
	
	public ModelAndView addObject(String attributeName, Object attributeValue) {
		getModelMap().addAttribute(attributeName, attributeValue);
		return this;
	}
	
	public ModelAndView addObject(Object attributeValue) {
		getModelMap().addAttribute(attributeValue);
		return this;
	}
	
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}
	
	public boolean isEmpty() {
		return this.view == null && CollectionUtils.isEmpty(model);
	}
	
	public boolean wasCleared() {
		return this.cleared && isEmpty();
	}
}
