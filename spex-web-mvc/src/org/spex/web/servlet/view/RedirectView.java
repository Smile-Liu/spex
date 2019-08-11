package org.spex.web.servlet.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.beans.BeanUtils;
import org.spex.http.HttpStatus;
import org.spex.util.ObjectUtils;
import org.spex.web.servlet.View;
import org.spex.web.util.WebUtils;

public class RedirectView extends AbstractUrlBasedView {

	private boolean contextRelative = false;
	
	private boolean http10Compatible = true;
	
	private boolean exposeModelAttribute = true;
	
	private String encodingSchema;
	
	private HttpStatus statusCode;
	
	
	public RedirectView() {}
	
	public RedirectView(String url) {
		super(url);
	}
	
	public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
		super(url);
		this.contextRelative = contextRelative;
		this.http10Compatible = http10Compatible;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		// 准备目标URL
		StringBuilder targetUrl = new StringBuilder();
		if (this.contextRelative && getUrl().startsWith("/")) {
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(getUrl());
		
		if (this.exposeModelAttribute) {
			String enc = this.encodingSchema;
			if (enc == null) {
				enc = request.getCharacterEncoding();
			}
			if (enc == null) {
				enc = WebUtils.DEFAULT_CHARACTOR_ENCODING;
			}
			appendQueryProperties(targetUrl, model, enc);
		}
		
		sendRedirect(request, response, targetUrl.toString(), this.http10Compatible);
	}
	
	protected void appendQueryProperties(StringBuilder targetUrl, Map<String, Object> model, String encodingSchema) throws UnsupportedEncodingException {
		
		// # 表示页面锚点的位置，先拿出来，最后再拼接上
		String fragment = null;
		int anchorIndex = targetUrl.indexOf("#");
		if (anchorIndex > -1) {
			fragment = targetUrl.substring(anchorIndex);
			targetUrl.delete(anchorIndex, targetUrl.length());
		}
		
		// 判断是否已经有参数了
		boolean first = getUrl().indexOf("?") < 0;
		for (Map.Entry<String, Object> entry : queryProperties(model).entrySet()) {
			Object rawValue = entry.getValue();
			Iterator<?> valueIter = null;
			if (rawValue != null && rawValue.getClass().isArray()) {
				valueIter = Arrays.asList(ObjectUtils.toObjectArray(rawValue)).iterator();
			} else if (rawValue instanceof Collection) {
				valueIter = ((Collection<?>) rawValue).iterator();
			} else {
				valueIter = Collections.singleton(rawValue).iterator();
			}
			while (valueIter.hasNext()) {
				Object value = valueIter.next();
				if (first) {
					targetUrl.append("?");
					first = false;
				} else {
					targetUrl.append("&");
				}
				String encodedKey = urlEncode(entry.getKey(), encodingSchema);
				String encodedValue = (value != null ? urlEncode(value.toString(), encodingSchema) : "");
				targetUrl.append(encodedKey).append("=").append(encodedValue);
			}
		}
		if (fragment != null) {
			targetUrl.append(fragment);
		}
	}
	
	protected Map<String, Object> queryProperties(Map<String, Object> model) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (isEligibleProperty(entry.getKey(), entry.getValue())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	protected boolean isEligibleProperty(String key, Object value) {
		if (value == null) {
			return false;
		}
		if (isEligibleValue(value)) {
			return true;
		}
		
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (length == 0) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				Object ele = Array.get(value, i);
				if (!isEligibleValue(ele)) {
					return false;
				}
			}
			return true;
		}
		
		if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			if (coll.isEmpty()) {
				return false;
			}
			for (Object ele : coll) {
				if (!isEligibleValue(ele)) {
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	protected boolean isEligibleValue(Object value) {
		return value != null && BeanUtils.isSimpleValueType(value.getClass());
	}
	
	protected String urlEncode(String input, String encodingSchema) throws UnsupportedEncodingException {
		return input != null ? URLEncoder.encode(input, encodingSchema) : null;
	}
	
	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, 
			String targetUrl, boolean http10Compatible) throws IOException {
		
		if (http10Compatible) {
			// 302
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		} else {
			HttpStatus status = getHttp11StatusCode(request, response, targetUrl);
			response.setStatus(status.value());
			response.setHeader("Location", response.encodeRedirectURL(targetUrl));
		}
	}
	
	protected HttpStatus getHttp11StatusCode(
			HttpServletRequest request, HttpServletResponse response, String targetUrl) {
		
		if (this.statusCode != null) {
			return statusCode;
		}
		
		HttpStatus attrStatusCode = (HttpStatus) request.getAttribute(View.RESPONSE_STATUS_ATTRIBUTE);
		if (attrStatusCode != null) {
			return attrStatusCode;
		}
		return HttpStatus.SEE_OTHER;
	}
}
