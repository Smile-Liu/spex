package org.spex.http;

public enum HttpStatus {
	
	CONTINUE(100),
	
	
	OK(200),
	
	
	SEE_OTHER(303),
	
	
	BAD_REQUEST(400),
	
	
	UNAUTHORIZED(401),
	
	
	FORBIDDEN(403),
	
	
	NOT_FOUND(404),
	
	
	INTERNAL_SERVER_ERROR(500),
	
	
	BAD_GATEWAY(502);
	
	
	private final int value;
	
	private HttpStatus(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
	
}
