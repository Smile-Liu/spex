package org.spex.beans.factory;

/**
 * BeanFactory总接口
 * 提供获取Bean的各种方法
 * 加载Bean是在子类的构造方法中实现的
 * @author hp
 */
public interface BeanFactory {
	
	String FACTORY_BEAN_PREFIX = "&";
	
	/**
	 * 通过BeanName获取Bean实例
	 * 仅支持name，不支持别名
	 * @param beanName 配置文件中指定的Bean名称
	 * @return Bean实例
	 */
	Object getBean(String beanName);
	
	
	<T> T getBean(String beanName, Class<T> requiredType);
	
	/**
	 * 通过classtype获取Bean实例
	 * 仅支持配置文件中的Class
	 * @param beanClass 配置文件中的Class
	 * @return Bean实例数组
	 */
	<T> T getBean(Class<T> beanClass);
	
	
	/**
	 * 判断是否包含指定BeanName的Bean
	 * 仅支持name，不支持别名
	 * @param beanName 配置文件中指定的Bean名称
	 * @return 是否包含
	 */
	boolean containsBean(String beanName);
	
	
	/**
	 * 根据名称（bean name, factory bean name）获取注册的Class
	 * @param name 名称
	 * @return
	 */
	Class<?> getType(String name);
	
	
	boolean isTypeMatch(String name, Class<?> targetType);
}
