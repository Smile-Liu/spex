package org.spex.context.event;

import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;
import org.spex.core.Ordered;

public class SourceFilteringListener implements SmartApplicationListener {

	private final Object source;
	
	private SmartApplicationListener delegate;
	
	public SourceFilteringListener(Object source, ApplicationListener<?> delegate) {
		this.source = source;
		this.delegate = (delegate instanceof SmartApplicationListener ? 
				(SmartApplicationListener) delegate : new GenericApplicationListenerAdapter(delegate));
	}
	
	public SourceFilteringListener(Object source) {
		this.source = source;
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (this.source == event.getSource()) {
			if (this.delegate == null) {
				throw new IllegalStateException("delegate is null");
			}
			this.delegate.onApplicationEvent(event);
		}
	}

	@Override
	public int getOrder() {
		return this.delegate != null ? this.delegate.getOrder() : Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public boolean supportEventType(Class<? extends ApplicationEvent> eventType) {
		return this.delegate == null || this.delegate.supportEventType(eventType);
	}

	@Override
	public boolean supportSourceType(Class<?> sourceType) {
		return sourceType.isInstance(source);
	}

}
