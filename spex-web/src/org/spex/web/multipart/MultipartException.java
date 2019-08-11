package org.spex.web.multipart;

public class MultipartException extends RuntimeException {

	private static final long serialVersionUID = 907265164539663853L;

	public MultipartException(String message) {
		super(message);
	}
	
	public MultipartException(String message, Throwable cause) {
		super(message, cause);
	}
}
