package org.spex.context;

public interface LifecycleProcessor extends Lifecycle {

	
	void onRefresh();
	
	void onClose();
}
