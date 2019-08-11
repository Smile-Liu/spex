package org.spex.beans.factory.xml;

import org.spex.beans.factory.support.DefaultListableBeanFactory;


public class XmlBeanFactory extends DefaultListableBeanFactory {

	private static final long serialVersionUID = 1L;
	
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
	
	public XmlBeanFactory(String resourceLocation) throws RuntimeException {
		this.reader.loadBeanDefinitions(resourceLocation);
	}

}
