package org.spex.context;

public interface SmartLifecycle extends Lifecycle, Phased {

	boolean isAutoStartup();
	
	void stop(Runnable callback);
}
