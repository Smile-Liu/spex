package org.spex.context.event;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.BeanFactoryAware;
import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationListener;
import org.spex.core.OrderComparator;

public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

	private final ListenerRetriever defaultRetriever = new ListenerRetriever();
	
	private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = 
		new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>();
	
	private BeanFactory beanFactory;
	
	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void addApplicationListenerBean(String listenerName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.add(listenerName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListenerBean(String listenerName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeAllListeners() {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.clear();
			this.defaultRetriever.applicationListeners.clear();
			this.retrieverCache.clear();
		}
	}

	public final void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	private BeanFactory getBeanFactory() {
		return this.beanFactory;
	}
	
	protected Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.defaultRetriever.getApplicationListeners();
	}
	
	protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event) {
		Class<? extends ApplicationEvent> eventType = event.getClass();
		Class<?> sourceType = event.getSource().getClass();
		
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		
		if (retriever != null) {
			return retriever.getApplicationListeners();
		}
		
		retriever = new ListenerRetriever();
		LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
		
		synchronized (this.defaultRetriever) {
			for (ApplicationListener<?> listener : this.defaultRetriever.applicationListeners) {
				if (supportEvent(listener, eventType, sourceType)) {
					retriever.applicationListeners.add(listener);
					allListeners.add(listener);
				}
			}
			
			if (!this.defaultRetriever.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				
				for (String listenerBeanName : this.defaultRetriever.applicationListenerBeans) {
					ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if (!allListeners.contains(listener) && supportEvent(listener, eventType, sourceType)) {
						allListeners.add(listener);
						retriever.applicationListeners.add(listener);
					}
				}
			}
			
			OrderComparator.sort(allListeners);
			return allListeners;
		}
	}
	
	protected boolean supportEvent(ApplicationListener<?> listener, 
			Class<? extends ApplicationEvent> eventType, Class<?> sourceType) {
		SmartApplicationListener smartListener = listener instanceof SmartApplicationListener ?
				(SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener);
		return smartListener.supportEventType(eventType) && smartListener.supportSourceType(sourceType);
	}
	
	private static class ListenerCacheKey {
		
		private final Class<?> eventType;
		
		private final Class<?> sourceType;
		
		public ListenerCacheKey(Class<?> eventType, Class<?> sourceType) {
			this.eventType = eventType;
			this.sourceType = sourceType;
		}

		@Override
		public int hashCode() {
			return eventType.hashCode() * 29 + sourceType.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}		
			ListenerCacheKey other = (ListenerCacheKey) obj;
			return this.eventType.equals(other.eventType) && this.sourceType.equals(other.sourceType);
		}
		
		
	}
	
	/**
	 * ¼àÌýÆ÷ÁÔÈ®
	 * @author hp
	 */
	private class ListenerRetriever {
		
		public final Set<ApplicationListener<?>> applicationListeners;
		
		public final Set<String> applicationListenerBeans;
		
		public ListenerRetriever() {
			this.applicationListeners = new LinkedHashSet<ApplicationListener<?>>();
			this.applicationListenerBeans = new LinkedHashSet<String>();
		}
		
		public Collection<ApplicationListener<?>> getApplicationListeners() {
			LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
			for (ApplicationListener<?> listener : applicationListeners) {
				allListeners.add(listener);
			}
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					allListeners.add(listener);
				}
			}
			OrderComparator.sort(allListeners);
			return allListeners;
		}
	}
}
