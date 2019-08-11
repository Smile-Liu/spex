package org.spex.beans.factory.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.beans.BeansException;
import org.spex.beans.PropertyEditorRegistry;
import org.spex.beans.TypeConverter;
import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.beans.factory.BeanNotOfRequiredTypeException;
import org.spex.beans.factory.CannotLoadBeanClassException;
import org.spex.beans.factory.DisposableBean;
import org.spex.beans.factory.FactoryBean;
import org.spex.beans.factory.ObjectFactory;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.spex.core.convert.ConversionService;
import org.spex.util.ClassUtils;

public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements BeanFactory {

	private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();
	
	private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<String, RootBeanDefinition>();
	
	/** 是否有BeanPostProcessor */
	private boolean hasInstantiationAwareBeanPostProcessors;
	
	/** 注册的BeanPostProcessor集合 */
	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();
	
	/** 自定义类型转换器 */
	private TypeConverter typeConverter;
	
	
	private ConversionService conversionService;
	
	/** 创建完成的bean name集合 */
	private Set<String> alreadyCreated = Collections.synchronizedSet(new HashSet<String>());

	@Override
	public Object getBean(String beanName) {
		return doGetBean(beanName, null, null);
	}

	@Override
	public <T> T getBean(String beanName, Class<T> requiredType) {
		return doGetBean(beanName, requiredType, null);
	}
	
	@Override
	public <T> T getBean(Class<T> beanClass) {
		return null;
	}

	@Override
	public boolean containsBean(String beanName) {
		return false;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	public ConversionService getConversionService() {
		return this.conversionService;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args) {
		
		final String beanName = transformedBeanName(name);
		Object bean = null;
		
		// 从缓存中获取实例（原则是开始创建实例时就暴露出来，别的来取时直接拿来用）
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			/*
			 * 为什么要加（args == null）条件呢？
			 * 因为参数不同，就是实例不同
			 */
			// 处理 FactoryBean 的 Bean Instance
			bean = getObjectForBeanInstance(sharedInstance, name, beanName);
		} else {
			// 仅支持单例，不支持Prototype
			
			final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			// 加载dependsOn，并注册
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String dependsOnBean : dependsOn) {
					getBean(dependsOnBean);
					registerDependentBean(dependsOnBean, beanName);
				}
			}
			
			if (mbd.isSingleton()) {
				sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {

					@Override
					public Object getObject() throws BeansException {
						return createBean(beanName, mbd, args);
					}
					
				});
				
				bean = getObjectForBeanInstance(sharedInstance, name, beanName);
			}
		}
		return (T) bean;
	}

	protected String transformedBeanName(String name) {
		return BeanFactoryUtils.transformedBeanName(name);
	}
	
	@SuppressWarnings("unchecked")
	protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName) {
		if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanNotOfRequiredTypeException(name, FactoryBean.class, beanInstance.getClass());
		}
		if (BeanFactoryUtils.isFactoryDereference(name) || !(beanInstance instanceof FactoryBean)) {
			return beanInstance;
		}
		
		// 能走到这步的，证明bean instance都满足 不以$开头；是FactoryBean的实例
		Object object = getCachedObjectForFactoryBean(beanName);
		if (object == null) {
			FactoryBean<Object> factory = (FactoryBean<Object>) beanInstance;
			object = getObjectFromFactoryBean(factory, beanName);
		}
		
		return object;
	}
	
	protected void initBeanWrapper(PropertyEditorRegistry registry) {
		
	}
	
	protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch) {
		try {
			if (mbd.hasBeanClass()) {
				return mbd.getBeanClass();
			}
			
			String className = mbd.getBeanClassName();
			if (className == null) {
				return null;
			}
			
			if (typesToMatch != null && typesToMatch.length > 0) {
				return null;
			} else {
				return getBeanClassLoader().loadClass(className);
			}
		} catch (ClassNotFoundException e) {
			throw new CannotLoadBeanClassException(beanName, mbd.getBeanClassName(), e);
		}
	}
	
	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}

	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		if (beanPostProcessor == null) {
			throw new IllegalArgumentException("'beanPostProcessor'参数为空");
		}
		
		this.beanPostProcessors.remove(beanPostProcessor);
		this.beanPostProcessors.add(beanPostProcessor);
		if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
			this.hasInstantiationAwareBeanPostProcessors = true;
		}
	}
	
	public boolean isBeanNameInUsed(String beanName) {
		return containsLocalBean(beanName) || hasDependentBeans(beanName);
	}
	
	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return (containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
				(!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
	}
	
	public boolean isFactoryBean(String name) {
		String beanName = transformedBeanName(name);
		
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			return beanInstance instanceof FactoryBean;
		}
		
		return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
	}
	
	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		Class<?> beanClass = predictBeanClass(beanName, mbd, FactoryBean.class);
		return beanClass != null && FactoryBean.class.isAssignableFrom(beanClass);
	}
	
	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) {
		RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
		if (mbd != null) {
			return mbd;
		}
		return new RootBeanDefinition(getBeanDefinition(beanName));
	}
	
	protected Class<?> predictBeanClass(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		if (mbd.getFactoryMethodName() != null) {
			return null;
		}
		return resolveBeanClass(mbd, beanName, typesToMatch);
	}
	
	/**
	 * 判断name是否对应FactoryBean，如果是，则返回FactoryBean是否是单例
	 * 如果不是，则返回对应beanInstance是否是单例
	 * @param name 原始name
	 * @return 是否
	 */
	public boolean isSingleton(String name) {
		String beanName = transformedBeanName(name);
		
		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				// 如果是FactoryBean，则返回FactoryBean是否是单例
				return BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton();
			} else {
				// 如果不是FactoryBean，则返回它不是
				return !BeanFactoryUtils.isFactoryDereference(name);
			}
		}
		else {
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			
			if (mbd.isSingleton()) {
				if (isFactoryBean(beanName, mbd)) {
					if (BeanFactoryUtils.isFactoryDereference(name)) {
						return true;
					}
					FactoryBean<?> factoryBean = (FactoryBean<?>) getBean("&" + beanName);
					return factoryBean.isSingleton();
				} else {
					return !BeanFactoryUtils.isFactoryDereference(name);
				}
			} else {
				return false;
			}
		}
		
	}
	
	public boolean isTypeMatch(String name, Class<?> targetType) {
		String beanName = transformedBeanName(name);
		Class<?> typeToMatch = targetType != null ? targetType : Object.class;
		
		// 只校验手动注册的单例
		// singletonObjectCache中存放原始对象，FactoryBean.getObject是在存缓存之后调的
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					Class<?> type = ((FactoryBean<?>) beanInstance).getObjectType();
					return type != null && typeToMatch.isAssignableFrom(type);
				} else {
					return typeToMatch.isAssignableFrom(beanInstance.getClass());
				}
			} else {
				return !BeanFactoryUtils.isFactoryDereference(name) && typeToMatch.isAssignableFrom(beanInstance.getClass());
			}
		} else {
			
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			Class<?> beanClass = predictBeanClass(beanName, mbd, FactoryBean.class, typeToMatch);
			if (beanClass == null) {
				return false;
			}
			
			// 是否是FactoryBean
			if (FactoryBean.class.isAssignableFrom(beanClass)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					Class<?> type = getTypeForFactoryBean(beanName, mbd);
					return type != null && typeToMatch.isAssignableFrom(type);
				} else {
					return typeToMatch.isAssignableFrom(beanClass);
				}
			} else {
				return !BeanFactoryUtils.isFactoryDereference(name) && typeToMatch.isAssignableFrom(beanClass);
			}
		}
	}
	
	public Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		try {
			FactoryBean<?> factoryBean = doGetBean("&" + beanName, FactoryBean.class, null);
			return factoryBean.getObjectType();
		} catch (BeansException e) {
			onSuppressedException(e);
			return null;
		}
	}
	
	public boolean hasInstantiationAwareBeanPostProcessors() {
		return hasInstantiationAwareBeanPostProcessors;
	}

	public List<BeanPostProcessor> getBeanPostProcessors() {
		return beanPostProcessors;
	}

	public TypeConverter getCustomTypeConverter() {
		return typeConverter;
	}

	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}

	public TypeConverter getTypeConverter() {
		TypeConverter customTypeConverter = getCustomTypeConverter();
		if (customTypeConverter != null) {
			return customTypeConverter;
		}
		return null;
	}
	
	protected void markBeanAsCreated(String beanName) {
		this.alreadyCreated.add(beanName);
	}
	
	protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
		if (!this.alreadyCreated.contains(beanName)) {
			removeSingleton(beanName);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 注册销毁事件
	 * 生成DisposableBean并缓存
	 * @param beanName
	 * @param bean
	 * @param mbd
	 */
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
		if (requireDestruction(bean, mbd)) {
			registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors()));
		}
	}
	
	/**
	 * 判断是否需要销毁
	 * @param bean
	 * @param mbd
	 * @return
	 */
	protected boolean requireDestruction(Object bean, RootBeanDefinition mbd) {
		return bean != null && (bean instanceof DisposableBean || mbd.getDestroyMethodName() != null);
	}
	
	protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
		return value;
	}
	
	public Class<?> getType(String name) {
		String beanName = transformedBeanName(name);
		
		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
			} else {
				return beanInstance.getClass();
			}
		} else {
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			Class<?> beanClass = predictBeanClass(beanName, mbd);
			
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					return getTypeForFactoryBean(beanName, mbd);
				} else {
					return beanClass;
				}
			} else {
				return BeanFactoryUtils.isFactoryDereference(name) ? null : beanClass;
			}
		}
	}
	
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);
	
	protected abstract boolean containsBeanDefinition(String beanName);
	
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
}
