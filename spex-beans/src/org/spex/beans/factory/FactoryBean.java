package org.spex.beans.factory;

public interface FactoryBean<T> {

	/**
	 * 返回被该工厂生成的Bean实例
	 * 如果调用该方法时，对应的FactoryBean还未实例化，则会抛出异常
	 * 运行返回null
	 * @return bean instance （能是null）
	 * @throws Exception
	 */
	T getObject() throws Exception;
	
	
	/**
	 * 返回被该工厂生成的Bean的类型
	 * @return
	 */
	Class<?> getObjectType();
	
	
	/**
	 * 判断被该工厂生成的Bean是否是单例
	 * 如果是，则通过 {@link #getObject()} 每次返回的都是同一个实例
	 * @return 是否是单例
	 */
	boolean isSingleton();
}
