package org.spex.web.servlet.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.beans.factory.BeanNameAware;
import org.spex.core.io.support.CollectionUtils;
import org.spex.web.context.support.WebApplicationObjectSupport;
import org.spex.web.servlet.View;
import org.spex.web.servlet.support.RequestContext;

public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	
	private static final int OUTPUT_BYTE_INITIAL_SIZE = 4096;
	
	private String beanName;
	
	private String contentType = DEFAULT_CONTENT_TYPE;
	
	private String requestContextAttribute;
	
	private final Map<String, Object> staticAttributes = new HashMap<String, Object>();
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	public String getRequestContextAttribute() {
		return requestContextAttribute;
	}

	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setAttributes(Properties attributes) {
		CollectionUtils.mergePropertiesIntoMap(attributes, staticAttributes);
	}
	
	public void setAttributesMap(Map<String, ?> attributes) {
		if (attributes != null) {
			for (Map.Entry<String, ?> entry : attributes.entrySet()) {
				addStaticAttribute(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void addStaticAttribute(String name, Object value) {
		this.staticAttributes.put(name, value);
	}
	
	public Map<String, Object> getStaticAttributes() {
		return staticAttributes;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> mergedModel = new HashMap<String, Object>(
				this.staticAttributes.size() + (model != null ? model.size() : 0));
		
		mergedModel.putAll(this.staticAttributes);
		if (model != null) {
			mergedModel.putAll(model);
		}
		
		if (this.requestContextAttribute != null) {
			mergedModel.put(requestContextAttribute, createRequestContext(request, response, mergedModel));
		}
		
		prepareResponse(request, response);
		renderMergedOutputModel(mergedModel, request, response);
	}

	public RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
		return new RequestContext(request, response, getServletContext(), model);
	}
	
	protected void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) {
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			String modelName = entry.getKey();
			Object modelValue = entry.getValue();
			if (modelValue != null) {
				request.setAttribute(modelName, modelValue);
			} else {
				request.removeAttribute(modelName);
			}
		}
	}
	
	protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
		if (generateDownloadContent()) {
			response.setHeader("Pragma", "private");
			response.setHeader("Cache-Control", "private, must-revalidate");
		}
	}
	
	protected boolean generateDownloadContent() {
		return false;
	}
	
	protected abstract void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
		throws Exception;
}
