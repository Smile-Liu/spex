package org.spex.context.event;

import java.util.concurrent.Executor;

import org.spex.beans.factory.BeanFactory;
import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;

public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	private Executor taskExecutor;
	
	public SimpleApplicationEventMulticaster() {}
	
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	protected Executor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void multicastEvent(final ApplicationEvent event) {
		for (final ApplicationListener listener : getApplicationListeners(event)) {
			Executor executor = getTaskExecutor();
			if (executor != null) {
				executor.execute(new Runnable() {
					
					public void run() {
						listener.onApplicationEvent(event);
					}
				});
			} else {
				listener.onApplicationEvent(event);
			}
		}
	}

}
