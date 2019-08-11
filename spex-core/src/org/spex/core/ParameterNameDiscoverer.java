package org.spex.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface ParameterNameDiscoverer {

	
	String[] getParameterNames(Method metod);
	
	
	String[] getParameterNames(Constructor<?> ctor);
	
}
