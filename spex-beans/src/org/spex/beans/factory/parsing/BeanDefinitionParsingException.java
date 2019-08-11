package org.spex.beans.factory.parsing;

public class BeanDefinitionParsingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BeanDefinitionParsingException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
