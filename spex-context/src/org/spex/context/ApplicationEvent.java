package org.spex.context;

import java.util.EventObject;

public abstract class ApplicationEvent extends EventObject {

	private static final long serialVersionUID = 4772063974895942052L;
	
	private final long timestamp;

	public ApplicationEvent(Object source) {
		super(source);
		timestamp = System.currentTimeMillis();
	}

	public long getTimestamp() {
		return timestamp;
	}

}
