package org.spex.web.context.support;

import java.io.IOException;

import org.spex.beans.BeansException;
import org.spex.beans.factory.support.DefaultListableBeanFactory;
import org.spex.beans.factory.xml.DelegatingXsdEntityResolver;
import org.spex.beans.factory.xml.XmlBeanDefinitionReader;


public class XmlWebApplicationContext extends AbstractRefreshableWebApplicationContext {

	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";
	
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";
	
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";
	
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {

		// XmlBeanDefinitionReader
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		
		// ≈‰÷√Œƒº˛(xml)Ω‚Œˆ∆˜
		beanDefinitionReader.setEntityResolver(new DelegatingXsdEntityResolver(this.getClass().getClassLoader()));
		
		// º”‘ÿ≈‰÷√
		loadBeanDefinitions(beanDefinitionReader);
	}

	
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (String configLocation : configLocations) {
				reader.loadBeanDefinitions(configLocation);
			}
		}
	}


	protected String[] getDefaultConfigLocations() {
		if (getNamespace() != null) {
			return new String[]{DEFAULT_CONFIG_LOCATION_PREFIX + getNamespace() + DEFAULT_CONFIG_LOCATION_SUFFIX};
		}
		else {
			return new String[] {DEFAULT_CONFIG_LOCATION};
		}
	}


	@Override
	public Object initializeBean(Object existingBean, String beanName)
			throws BeansException {
		return getBeanFactory().initializeBean(existingBean, beanName);
	}


}
