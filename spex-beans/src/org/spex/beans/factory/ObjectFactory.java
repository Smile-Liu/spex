package org.spex.beans.factory;

import org.spex.beans.BeansException;

public interface ObjectFactory<T> {

	T getObject() throws BeansException;
}
