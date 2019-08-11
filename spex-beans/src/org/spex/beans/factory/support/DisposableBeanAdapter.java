package org.spex.beans.factory.support;

import java.io.Serializable;
import java.util.List;

import org.spex.beans.factory.DisposableBean;
import org.spex.beans.factory.config.BeanPostProcessor;

public class DisposableBeanAdapter implements DisposableBean, Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition mbd,
			List<BeanPostProcessor> processors) {
		
	}
	
	@Override
	public void run() {
		destroy();
	}

	@Override
	public void destroy() {
		
	}

}
