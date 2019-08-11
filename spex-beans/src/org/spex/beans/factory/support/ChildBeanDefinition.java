package org.spex.beans.factory.support;

import org.spex.beans.factory.config.AbstractBeanDefinition;

public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
}
