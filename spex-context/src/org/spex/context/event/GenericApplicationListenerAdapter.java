package org.spex.context.event;

import org.spex.aop.support.AopUtils;
import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;
import org.spex.core.GenericTypeResolver;
import org.spex.core.Ordered;

public class GenericApplicationListenerAdapter implements SmartApplicationListener {

	@SuppressWarnings("rawtypes")
	private final ApplicationListener delegate;
	
	public GenericApplicationListenerAdapter(ApplicationListener<?> delegate) {
		this.delegate = delegate;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	@Override
	public int getOrder() {
		return this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public boolean supportEventType(Class<? extends ApplicationEvent> eventType) {
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
		if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
			// 实际上就是delegate.getClass
			Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
			if (targetClass != this.delegate.getClass()) {
				typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
			}
		}
		return typeArg == null || typeArg.isAssignableFrom(eventType);
	}

	@Override
	public boolean supportSourceType(Class<?> sourceType) {
		return true;
	}

}
