package org.spex.context;

/**
 * 事件发布器
 * @author hp
 *
 */
public interface ApplicationEventPublisher {

	void publishEvent(ApplicationEvent event);
}
