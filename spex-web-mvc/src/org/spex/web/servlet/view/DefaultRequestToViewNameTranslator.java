package org.spex.web.servlet.view;

import javax.servlet.http.HttpServletRequest;

import org.spex.util.StringUtils;
import org.spex.web.servlet.RequestToViewNameTranslator;
import org.spex.web.util.UrlPathHelper;

public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

	private final static String SLASH = "/";
	
	
	private String prefix = "";
	
	private String suffix = "";
	
	private String separator = SLASH;
	
	private boolean stripLeadingSlash = true;
	
	private boolean stripTrailingSlash = true;
	
	private boolean stripExtension = true;
	
	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	public void setPrefix(String prefix) {
		this.prefix = prefix != null ? prefix : "";
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix != null ? suffix : "";
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setStripLeadingSlash(boolean stripLeadingSlash) {
		this.stripLeadingSlash = stripLeadingSlash;
	}

	public void setStripTrailingSlash(boolean stripTrailingSlash) {
		this.stripTrailingSlash = stripTrailingSlash;
	}

	public void setStripExtension(boolean stripExtension) {
		this.stripExtension = stripExtension;
	}

	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		if (urlPathHelper == null) {
			throw new IllegalStateException("UrlPathHelper²»ÄÜÎª¿Õ");
		}
		this.urlPathHelper = urlPathHelper;
	}

	@Override
	public String getViewName(HttpServletRequest request) throws Exception {
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		return this.prefix + transformPath(lookupPath) + this.suffix;
	}
	
	protected String transformPath(String lookupPath) {
		String path = lookupPath;
		if (this.stripLeadingSlash && path.startsWith(SLASH)) {
			path = path.substring(1);
		}
		if (this.stripTrailingSlash && path.endsWith(SLASH)) {
			path = path.substring(0, path.length() - 1);
		}
		if (this.stripExtension) {
			path = StringUtils.stripFilenameExtension(path);
		}
		if (!SLASH.equals(this.separator)) {
			path = StringUtils.replace(path, SLASH, this.separator);
		}
		return path;
	}
}
