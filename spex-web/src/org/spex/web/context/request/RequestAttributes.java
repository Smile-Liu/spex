package org.spex.web.context.request;

public interface RequestAttributes {

	int SCOPE_REQUEST = 0;
	
	int SCOPE_SESSION = 1;
	
	int SCOPE_GLOBAL_SESSION = 2;
	
	Object getAttribute(String name, int scope);
	
	void setAttribute(String name, Object value, int scope);
	
	void removeAttribute(String name, int scope);
}
