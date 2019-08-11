package org.spex.context.support;

import org.spex.beans.factory.BeanNameAware;
import org.spex.beans.factory.InitializingBean;
import org.spex.context.ApplicationContext;
import org.spex.util.StringUtils;

public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext 
		implements BeanNameAware, InitializingBean {

	private String[] configLocations;
	private boolean setIdCalled = false;
	
	public AbstractRefreshableConfigApplicationContext() {
		
	}
	
	public AbstractRefreshableConfigApplicationContext(ApplicationContext parent) {
		super(parent);
	}
	
	public void setConfigLocation(String configLocation) {
		setConfigLocations(StringUtils.tokenizeToStringArray(configLocation, CONFIG_LOCATION_DELIMITTERS));
	}

	public void setConfigLocations(String[] locations) {
		if (locations != null) {
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = locations[i];
			}
		} else {
			this.configLocations = null;	
		}
	}
	
	public String[] getConfigLocations() {
		return this.configLocations != null ? this.configLocations : getDefaultConfigLocations();
	}
	
	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}
	
	/**
	 * @see org.spex.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String beanName) {
		if (!this.setIdCalled) {
			super.setId(beanName);
			setDisplayName("ApplicationContext '" + beanName + "'");
		}
	}
	
	/**0
	 * @see org.spex.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (!isActive()) {
			refresh();
		}
	}
	
	protected String[] getDefaultConfigLocations() {
		return null;
	}
}
