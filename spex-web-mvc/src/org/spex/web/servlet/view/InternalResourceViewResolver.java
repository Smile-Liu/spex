package org.spex.web.servlet.view;

public class InternalResourceViewResolver extends UrlBasedViewResolver {

	private Boolean alwaysInclude;
	
	private Boolean exposeContextBeansAsAttributes;
	
	private String[] exposedContextBeanNames;
	
	public InternalResourceViewResolver() {
		setViewClass(InternalResourceView.class);
	}

	public boolean isAlwaysInclude() {
		return alwaysInclude;
	}

	public void setAlwaysInclude(Boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	public boolean isExposeContextBeansAsAttributes() {
		return exposeContextBeansAsAttributes;
	}

	public void setExposeContextBeansAsAttributes(
			Boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
	}

	public String[] getExposedContextBeanNames() {
		return exposedContextBeanNames;
	}

	public void setExposedContextBeanNames(String[] exposedContextBeanNames) {
		this.exposedContextBeanNames = exposedContextBeanNames;
	}
	
	@Override
	public AbstractUrlBasedView buildView(String viewName) throws Exception {
		InternalResourceView view = (InternalResourceView) super.buildView(viewName);
		if (this.alwaysInclude != null) {
			view.setAlwaysInclude(isAlwaysInclude());
		}
		if (this.exposeContextBeansAsAttributes != null) {
			view.setExposeContextBeansAsAttributes(isExposeContextBeansAsAttributes());
		}
		view.setPreventDispatchLoop(true);
		return view;
	}
	
}
