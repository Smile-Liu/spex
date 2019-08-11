package org.spex.web.servlet.view;

import org.spex.beans.factory.InitializingBean;

public abstract class AbstractUrlBasedView extends AbstractView implements InitializingBean {

	private String url;
	
	public AbstractUrlBasedView() {}
	
	public AbstractUrlBasedView(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getUrl() == null) {
			throw new IllegalArgumentException("url±ÿ ‰");
		}
	}
	
}
