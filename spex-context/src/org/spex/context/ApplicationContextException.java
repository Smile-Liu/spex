package org.spex.context;

import org.spex.beans.BeansException;

public class ApplicationContextException extends BeansException {

	private static final long serialVersionUID = -2650984368660967014L;

	public ApplicationContextException(String msg) {
		super(msg);
	}

	public ApplicationContextException(String msg, Throwable e) {
		super(msg, e);
	}
}
