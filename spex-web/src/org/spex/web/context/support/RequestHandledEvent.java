package org.spex.web.context.support;

import org.spex.context.ApplicationEvent;

public class RequestHandledEvent extends ApplicationEvent {

	private static final long serialVersionUID = 8771828832367648210L;

	private String sessionId;
	
	private String userName;
	
	private final long processingTimeMillis;
	
	private Throwable failureCause;
	
	public RequestHandledEvent(Object source, String sessionId, String userName, long processingTimeMillis) {
		super(source);
		this.sessionId = sessionId;
		this.userName = userName;
		this.processingTimeMillis = processingTimeMillis;
	}
	
	public RequestHandledEvent(Object source, String sessionId, String userName, long processingTimeMillis, Throwable ex) {
		this(source, sessionId, userName, processingTimeMillis);
		this.failureCause = ex;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getUserName() {
		return userName;
	}

	public Throwable getFailureCause() {
		return failureCause;
	}

	public long getProcessingTimeMillis() {
		return processingTimeMillis;
	}
	
	public boolean wasFailure() {
		return this.failureCause != null;
	}
	
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("session=[").append(this.sessionId).append("];");
		sb.append("user=[").append(this.userName).append("];");
		return sb.toString();
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("session=[").append(this.sessionId).append("];");
		sb.append("user=[").append(this.userName).append("];");
		sb.append("time=[").append(this.processingTimeMillis).append("ms];");
		sb.append("status=[");
		if (!wasFailure()) {
			sb.append("OK");
		} else {
			sb.append("failure: ").append(this.failureCause);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String toString() {
		return "RequestHandledEvent£º" + getDescription();
	}
}
