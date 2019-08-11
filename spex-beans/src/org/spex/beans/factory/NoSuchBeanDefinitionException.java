package org.spex.beans.factory;

import org.spex.beans.BeansException;

public class NoSuchBeanDefinitionException extends BeansException {

	private static final long serialVersionUID = -6327814479780951456L;

	public NoSuchBeanDefinitionException(String name) {
		super("δ�ҵ�����Ϊ" + name + "��bean");
	}

	public NoSuchBeanDefinitionException(String name, String message) {
		super("δ�ҵ�����Ϊ" + name + "��bean��" + message);
	}

	public NoSuchBeanDefinitionException(Class<?> type) {
		super("δ�ҵ�����Ϊ" + type.getName() + "��bean");
	}

	public NoSuchBeanDefinitionException(Class<?> type, String message) {
		super("δ�ҵ�����Ϊ" + type.getName() + "��bean��" + message);
	}
}
