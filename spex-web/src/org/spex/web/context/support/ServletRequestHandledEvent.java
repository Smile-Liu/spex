package org.spex.web.context.support;

public class ServletRequestHandledEvent extends RequestHandledEvent {

	private static final long serialVersionUID = 5014037862417633595L;

	private final String requestUrl;
	
	private final String clientAddress;
	
	private final String method;
	
	private final String servletName;
	
	public ServletRequestHandledEvent(Object source, String sessionId, String userName, long processingTimeMillis,
			String requestUrl, String clientAddress, String method, String servletName) {
		super(source, sessionId, userName, processingTimeMillis);
		this.requestUrl = requestUrl;
		this.clientAddress = clientAddress;
		this.method = method;
		this.servletName = servletName;
	}
	
	public ServletRequestHandledEvent(Object source, String sessionId, String userName, long processingTimeMillis,
			Throwable failure, String requestUrl, String clientAddress, String method, String servletName) {
		super(source, sessionId, userName, processingTimeMillis, failure);
		this.requestUrl = requestUrl;
		this.clientAddress = clientAddress;
		this.method = method;
		this.servletName = servletName;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public String getMethod() {
		return method;
	}

	public String getServletName() {
		return servletName;
	}
	
	@Override
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("url=[").append(this.requestUrl).append("];");
		sb.append("client=[").append(this.clientAddress).append("];");
		sb.append(super.getShortDescription());
		return sb.toString();
	}
	
	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("url=[").append(this.requestUrl).append("];");
		sb.append("client=[").append(this.clientAddress).append("];");
		sb.append("method=[").append(this.method).append("];");
		sb.append("servlet=[").append(this.servletName).append("];");
		sb.append(super.getDescription());
		return sb.toString();
	}

	@Override
	public String toString() {
		return "ServletRequestHandledEvent:" + getDescription();
	}
}
