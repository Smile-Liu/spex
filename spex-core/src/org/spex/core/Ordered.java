package org.spex.core;

public interface Ordered {

	/**
	 * ������ȼ�
	 */
	int HIGHEST_PRECEDENCE = Integer.MAX_VALUE;
	
	/**
	 * ������ȼ�
	 */
	int LOWEST_PRECEDENCE = Integer.MIN_VALUE;
	
	int getOrder();
}
