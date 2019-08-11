package org.spex.beans.factory;

public interface InitializingBean {

	void afterPropertiesSet() throws Exception;
}
