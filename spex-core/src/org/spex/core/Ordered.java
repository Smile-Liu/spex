package org.spex.core;

public interface Ordered {

	/**
	 * 最高优先级
	 */
	int HIGHEST_PRECEDENCE = Integer.MAX_VALUE;
	
	/**
	 * 最低优先级
	 */
	int LOWEST_PRECEDENCE = Integer.MIN_VALUE;
	
	int getOrder();
}
