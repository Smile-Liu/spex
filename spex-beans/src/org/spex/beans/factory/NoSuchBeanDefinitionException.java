package org.spex.beans.factory;

import org.spex.beans.BeansException;

public class NoSuchBeanDefinitionException extends BeansException {

	private static final long serialVersionUID = -6327814479780951456L;

	public NoSuchBeanDefinitionException(String name) {
		super("未找到名称为" + name + "的bean");
	}

	public NoSuchBeanDefinitionException(String name, String message) {
		super("未找到名称为" + name + "的bean：" + message);
	}

	public NoSuchBeanDefinitionException(Class<?> type) {
		super("未找到类型为" + type.getName() + "的bean");
	}

	public NoSuchBeanDefinitionException(Class<?> type, String message) {
		super("未找到类型为" + type.getName() + "的bean：" + message);
	}
}
