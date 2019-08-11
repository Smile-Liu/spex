package org.spex.context;

public interface Lifecycle {

	void start();
	
	void stop();
	
	boolean isRunning();
}
