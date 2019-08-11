package org.spex.beans.factory;

public interface SmartFactoryBean<T> extends FactoryBean<T> {
	
	boolean isEagerInit();

}
