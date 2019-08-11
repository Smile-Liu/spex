package org.spex.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.beans.BeanUtils;
import org.spex.beans.BeanWrapper;
import org.spex.beans.BeanWrapperImpl;
import org.spex.beans.BeansException;
import org.spex.beans.PropertyAccessorUtils;
import org.spex.beans.PropertyValue;
import org.spex.beans.PropertyValues;
import org.spex.beans.TypeConverter;
import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.BeanCurrentlyInCreationException;
import org.spex.beans.factory.BeanDefinitionStoreException;
import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.BeanFactoryAware;
import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.beans.factory.FactoryBean;
import org.spex.beans.factory.InitializingBean;
import org.spex.beans.factory.ListableBeanFactory;
import org.spex.beans.factory.MutablePropertyValues;
import org.spex.beans.factory.NoSuchBeanDefinitionException;
import org.spex.beans.factory.ObjectFactory;
import org.spex.beans.factory.SmartFactoryBean;
import org.spex.beans.factory.config.AbstractBeanDefinition;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.beans.factory.config.DependencyDescriptor;
import org.spex.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.spex.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.spex.beans.factory.config.TypedStringValue;
import org.spex.core.MethodParameter;
import org.spex.core.ParameterNameDiscoverer;
import org.spex.core.PriorityOrdered;
import org.spex.util.ClassUtils;
import org.spex.util.LoggerUtil;
import org.spex.util.ObjectUtils;
import org.spex.util.ReflectionUtils;
import org.spex.util.StringUtils;

public class DefaultListableBeanFactory extends AbstractBeanFactory implements ListableBeanFactory, BeanDefinitionRegistry, Serializable {

	private static final long serialVersionUID = 1L;
	
	/** bean definition cache */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();
	
	/** bean definition names 列表 */
	private final List<String> beanDefinitionNames = new ArrayList<String>();
	
	private ParameterNameDiscoverer parameterNameDiscoverer;
	
	private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();
	
	/** 允许自动处理循环依赖 */
	private boolean allowCircularReference = true;
	
	/** 允许初始状态被依赖后的再包装 */
	private boolean allowRawInjectionDespiteWrapping = false;
	
	/** 忽略 */
	private final Set<Class<?>> ignoreDependencyTypes = new HashSet<Class<?>>();
	private final Set<Class<?>> ignoreDependencyInterfaces = new HashSet<Class<?>>();
	
	private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();
	
	private boolean allowEagerClassLoading = true;
	
	/** 缓存依赖类型的相应的自动装配的值 */
	private final Map<Class<?>, Object> resolvableDependencies = new HashMap<Class<?>, Object>();
	
	private boolean configurationFrozen = false;
	
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
		if (!StringUtils.hasText(beanName)) {
			throw new IllegalArgumentException("Bean Name 不能为空");
		}
		if (beanDefinition == null) {
			throw new IllegalArgumentException("Bean Definition 不能为空");
		}
		
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			} catch (BeanDefinitionValidationException e) {
				throw new BeanDefinitionStoreException("Bean Definition验证失败", e, beanName);
			}
		}
		
		synchronized (this.beanDefinitionMap) {
			if (this.beanDefinitionMap.containsKey(beanName)) {
				throw new BeanDefinitionStoreException("注册Bean Definition " + beanName + "失败，因为已经存在了");
			}
			this.beanDefinitionNames.add(beanName);
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
	}

	@Override
	public void removeBeanDefinition(String beanName) {
		
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) {
		BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
		if (beanDefinition == null) {
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return beanDefinition;
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}
	
	@Override
	protected Object createBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
		
		resolveBeanClass(mbd, beanName);
		
		// 不支持 lookupMethod和replacedMethod，所以没有methodOverrides
		
		try {
			// 执行一次 BeanPostProcessor
			Object bean = resolveBeforeInstantiation(beanName, mbd);
			
			// 如果在实例化前，执行的BeanPostProcessor有结果，则中断并返回结果
			if (bean != null) {
				return bean;
			}
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "BeanPostProcessor在bean实例化前 before instantiation 执行失败", e);
		}
		
		// 正常进行实例化
		Object beanInstance = doCreateBean(beanName, mbd, args);
		return beanInstance;
	}

	protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
		BeanWrapper instanceWrapper = createBeanInstance(beanName, mbd, args);
		
		final Object bean = instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null;
//		Class<?> beanType = instanceWrapper != null ? instanceWrapper.getWrappedClass() : null;
		
		// 不支持 MergedBeanDefinitionPostProcessor的BeanPostProcessor
		
		// 解决循环依赖
		// bean正在被创建，并且允许自动处理，则把正在创建的bean放入ObjectFactory，暴露出去
		// 当有依赖bean的Bean实例化时，在getSingleton()方法中直接从singletonFactory缓存中取得正在创建的bean的引用
		boolean earlySingletonExposure = mbd.isSingleton() && this.allowCircularReference &&
				isSingletonCurrentlyInCreation(beanName);
		if (earlySingletonExposure) {
			addSingletonFactory(beanName, new ObjectFactory<Object>() {

				@Override
				public Object getObject() throws BeansException {
					return getEarlyBeanReference(beanName, mbd, bean);
				}
				
			});
		}
		
		Object exposedObject = bean;
		
		// 填充属性到BeanWrapper
		populateBean(beanName, mbd, instanceWrapper);
		
		// 初始化
		if (exposedObject != null) {
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		
		// 校验是否有依赖该bean的初始版本，但bean最终被包装，而导致版本不一致
		if (earlySingletonExposure) {
			// 这个reference就是前面暴露出去的bean，正常情况和初始化前的exposedObject是一样的
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					// 最终版本和初始版本一致，则没有问题
					exposedObject = earlySingletonReference;
				} else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new HashSet<String>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName, 
								"bean '" + beanName + "'的初始版本被其他bean依赖，但最终该bean被包装了。" +
								"也就是说，其他bean不需要最终版本的这个bean。这经常是因为过早的进行类型匹配了。" +
								"可以试试在 'getBeanNamesOfType'执行时把'allowEagerInit'属性置为false");
					}
				}
			}
		}
		
		// 注册注销
		registerDisposableBeanIfNecessary(beanName, bean, mbd);
		
		return exposedObject;
	}
	
	
	protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
		// 针对不同类型的Aware注入对应的值
		// BeanNameAware <- beanName
		// BeanClassLoaderAware <- classLoader
		// BeanFactoryAware <- AbstractAutowireCapableBeanFactory.class
		invokeAwareMethod(beanName, bean);
		
		Object wrappedBean = bean;
		
		// 调用后置处理器的before initialization
		if (mbd == null) {
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(beanName, wrappedBean);
		}
		
		// 实现初始化，包含两部分：实现了InitializingBean接口、配置了init-method
		try {
			invokeInitMethods(beanName, wrappedBean, mbd);
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "调用init-method失败", e);
		}
		
		// 调用后置处理器的after initialization
		if (mbd == null) {
			wrappedBean = applyBeanPostProcessorsAfterInitialization(beanName, wrappedBean);
		}
		
		return wrappedBean;
	}
	
	/**
	 * 实现初始化
	 * 1.实现了InitializingBean接口的调用afterPropertiesSet()
	 * 2.配置了init-method的进行调用指定的方法
	 * @param beanName
	 */
	protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
		
		// InitializingBean
		boolean isInitializingBean = bean instanceof InitializingBean;
		if (isInitializingBean) {
			((InitializingBean) bean).afterPropertiesSet();
		}
		
		// init-method
		if (mbd != null) {
			String initMethodName = mbd.getInitMethodName();
			if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName))) {
				invokeCustomInitMethod(beanName, bean, mbd);
			}
		}
	}
	
	protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
		String initMethodName = mbd.getInitMethodName();

		// 校验init-method是否存在
		final Method initMethod = (mbd.isNonPublicAccessAllowed() ? 
				BeanUtils.findMethod(bean.getClass(), initMethodName) :
				ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));
		if (initMethod == null) {
			// 不做对初始化方法的强制要求
			return ;
		}
		
		try {
			// 调用
			ReflectionUtils.makeAccessible(initMethod);
			initMethod.invoke(bean);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (IllegalAccessException e) {
			throw e;
		}
	}
	
	protected void populateBean(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw) {
		// 配置文件中定义的property子标签
		PropertyValues pvs = mbd.getPropertyValues();
		
		if (bw == null) {
			if (!pvs.isEmpty()) {
				throw new BeanCreationException(beanName, "不能把property填充到空的bean实例上");
			} else {
				return;
			}
		}
		
		// 在填充属性前，允许InstantiationAwareBeanPostProcessor去修改bean instance的状态
		boolean continueWithPropertyPopulation = true;
		
		if (hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
						continueWithPropertyPopulation = false;
						break;
					}
				}
			}
		}
		
		if (!continueWithPropertyPopulation) {
			return;
		}
		
		if (mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_BY_NAME ||
				mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
			
			if (mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}
			
			if (mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}
			
			pvs = newPvs;
		}
		
		// 
		boolean hasInstantiationAwareBpps = hasInstantiationAwareBeanPostProcessors();
		if (hasInstantiationAwareBpps) {
			PropertyDescriptor[] filteredPds = filterPropertyDescriptors(bw);
			for (BeanPostProcessor bpp : getBeanPostProcessors()) {
				if (bpp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bpp;
					pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
					if (pvs == null) {
						return;
					}
				}
			}
		}
		
		// 应用属性
		applyPropertyValues(beanName, mbd, bw, pvs);
	}
	
	protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
		if (pvs == null || pvs.isEmpty()) {
			return ;
		}
		
		MutablePropertyValues mpvs = null;
		List<PropertyValue> original;
		
		if (pvs instanceof MutablePropertyValues) {
			mpvs = (MutablePropertyValues) pvs;
			if (mpvs.isConverted()) {
				try {
					bw.setPropertyValues(mpvs);
					return;
				} catch (BeansException e) {
					throw new BeanCreationException(beanName, "填充属性值时发生错误", e);
				}
			}
			original = mpvs.getPropertyValueList();
		} else {
			original = Arrays.asList(pvs.getPropertyValues());
		}
		
		// 类型转化器
		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);
		// 把原始数据复制到另一个地方
		List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
		boolean resolveNecessary = false;
		
		for (PropertyValue pv : original) {
			if (pv.isConverted()) {
				deepCopy.add(pv);
			} else {
				String propertyName = pv.getName();
				Object originalValue = pv.getValue();
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;
				boolean convertible = bw.isWritableProperty(propertyName) && 
					!PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
				if (convertible) {
					convertedValue = convertedForProperty(resolvedValue, propertyName, bw, converter);
				}
				
				// 记录转换后的值，防止重复转换
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				} else if ((convertible && originalValue instanceof TypedStringValue &&
						!((TypedStringValue) originalValue).isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue)))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				} else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		if (mpvs != null && !resolveNecessary) {
			mpvs.setConverted();
		}
		bw.setPropertyValues(new MutablePropertyValues(deepCopy));
	}
	
	protected PropertyDescriptor[] filterPropertyDescriptors(BeanWrapper bw) {
		return null;
	}
	
	protected void autowireByType(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw,
			MutablePropertyValues pvs) {
		
		TypeConverter typeConverter = getCustomTypeConverter();
		if (typeConverter == null) {
			typeConverter = bw;
		}
		
		Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
		
		// 按属性名找到实例需要但是没有配置的属性项，
		// 然后解析属性的类型，再根据类型去查找配置的对应的beanName
		// 然后根据bean name获得bean，一起组装成一个PropertyValue，放入pvs
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			
			// 永远不要自动注入类型Object，即使它是一个不明确的非简单类型的属性
			if (!Object.class.equals(pd.getPropertyType())) {
				// 获取属性的setter方法参数
				MethodParameter methodParam = BeanUtils.getWriterMethodParameter(pd);
				
				boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
				// 依赖描述器
				DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
				
				// * 解析
				Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
				if (autowiredArgument != null) {
					pvs.add(propertyName, autowiredArgument);
				}
				for (String autowiredBeanName : autowiredBeanNames) {
					registerDependentBean(autowiredBeanName, beanName);
				}
				autowiredBeanNames.clear();
			}
		}
	}
	
	public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
		if (dependencyType != null && autowiredValue != null) {
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}
	
	public Object resolveDependency(DependencyDescriptor desc, String beanName, Set<String> autowiredBeanNames, 
			TypeConverter converter) {
		
		desc.initParameterNameDiscover(getParameterNameDiscoverer());
		if (desc.getDependencyType().equals(ObjectFactory.class)) {
			return new DependencyObjectFactory(desc, beanName);
		} else {
			return doResolveDependency(desc, desc.getDependencyType(), beanName, autowiredBeanNames, converter);
		}
	}
	
	protected Object doResolveDependency(DependencyDescriptor desc, Class<?> type, String beanName, Set<String> autowiredBeanNames, 
			TypeConverter converter) {
		
		Object value = getAutowireCandidateResolver().getSuggestedValue(desc);
		if (value != null) {
			// 现在都为空
		}
		
		if (type.isArray()) {
			// 依赖项的类型是数组
			Class<?> componentType = type.getComponentType();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, desc);
			if (matchingBeans.isEmpty()) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(componentType, "期望能有至少一个bean可以匹配到该依赖，但是未找到");
				}
				return null;
			}
			
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			
			converter = converter != null ? converter : getTypeConverter();
			return converter.convertIfNecessary(matchingBeans.values(), type);
			
		} else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> elementType = desc.getCollectionType();
			if (elementType == null) {
				if (desc.isRequired()) {
					throw new BeansException("没有为集合[" + type.getName() + "]定义元素类型");
				}
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, desc);
			if (matchingBeans.isEmpty()) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(elementType, "集合类型为[" + elementType.getName() + "]" +
							"期望能有至少一个bean可以匹配到该依赖，但是未找到");
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			converter = converter != null ? converter : getTypeConverter();
			return converter.convertIfNecessary(matchingBeans.values(), type);
			
		} else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> mapKeyType = desc.getMapKeyType();
			if (mapKeyType == null || !String.class.isAssignableFrom(mapKeyType)) {
				if (desc.isRequired()) {
					throw new BeansException("Map [" + type.getName() + "]的键（K）类型[" + mapKeyType + "只能是String");
				}
				return null;
			}
			
			Class<?> mapValueType = desc.getMapValueType();
			if (mapValueType == null) {
				if (desc.isRequired()) {
					throw new BeansException("Map [" + type.getName() + "]中未定义值（V）的类型");
				}
				return null;
			}
			
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, mapValueType, desc);
			if (matchingBeans == null) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(mapValueType, "期望能有至少一个bean" +
							"（值类型为[" + mapValueType.getName() + "]）可以匹配到该依赖，但是未找到");
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		} else {
			// 其他类型，返回一个值
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, desc);
			if (matchingBeans == null) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(type, "期望能有至少一个bean可以匹配到该依赖，但是未找到");
				}
				return null;
			}
			if (matchingBeans.size() > 1) {
				String primaryBeanName = determinePrimaryCandidate(matchingBeans, desc);
				if (primaryBeanName == null) {
					throw new NoSuchBeanDefinitionException(type, "期望能有一个bean可以匹配到该依赖，" +
							"但是找到" + matchingBeans.size() + "个：" + matchingBeans.keySet());
				}
				if (autowiredBeanNames != null) {
					autowiredBeanNames.add(primaryBeanName);
				}
				return matchingBeans.get(primaryBeanName);
			}
			Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(entry.getKey());
			}
			return entry.getValue();
		}
		
	}
	
	protected String determinePrimaryCandidate(Map<String, Object> candidateBeans, DependencyDescriptor descriptor) {
		String primaryBeanName = null;
		String fallbackBeanName = null;
		for (Map.Entry<String, Object> entry : candidateBeans.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					if (candidateLocal == primaryLocal) {
						throw new NoSuchBeanDefinitionException(descriptor.getDependencyType(), 
								"对于候选依赖" + candidateBeans.keySet() + "找到多个'primary'bean");
					} else if (candidateLocal && !primaryLocal) {
						primaryBeanName = candidateBeanName;
					}
				} else {
					primaryBeanName = candidateBeanName;
				}
			}
			if (primaryBeanName == null && 
					(this.resolvableDependencies.values().contains(beanInstance) || 
							candidateBeanName.equals(descriptor.getDependencyName()))) {
				fallbackBeanName = candidateBeanName;
			}
		}
		return primaryBeanName != null ? primaryBeanName : fallbackBeanName;
	}
	
	protected boolean isPrimary(String beanName, Object beanInstance) {
		if (containsBeanDefinition(beanName)) {
			return getMergedLocalBeanDefinition(beanName).isPrimary();
		} else {
			return false;
		}
	}
	
	protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType, 
			DependencyDescriptor descriptor) {
		
		String[] candidateNames = 
			BeanFactoryUtils.beanNameForTypeIncludingAncestors(this, requiredType, true, descriptor.isEager());
		Map<String, Object> result = new LinkedHashMap<String, Object>(candidateNames.length);
		
		for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = this.resolvableDependencies.get(autowiringType);
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		
		for (String candidate : candidateNames) {
			if (!candidate.equals(beanName) && isAutowireCandidate(candidate, descriptor)) {
				result.put(candidate, getBean(candidate));
			}
		}
		
		return result;
	}
	
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {
		
		// 依赖的类型是否是FactoryBean
		boolean isFactoryBean = descriptor != null && descriptor.getDependencyType() != null && 
			FactoryBean.class.isAssignableFrom(descriptor.getDependencyType());
		if (isFactoryBean) {
			beanName = BeanFactoryUtils.transformedBeanName(beanName);
		}
		
		if (containsBeanDefinition(beanName)) {
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanName), descriptor);
		} else if (containsSingleton(beanName)) {
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor);
		} else {
			return true;
		}
	}
	
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor) {
		resolveBeanClass(mbd, beanName);
		
		if (mbd.isFactoryMethodUnique && mbd.resolvedConstructorOrFactoryMethod == null) {
			// 给resolvedConstructorOrFactoryMethod赋值：遍历所有方法，找到唯一匹配的一个
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		return getAutowireCandidateResolver().isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName), descriptor);
	}
	
	protected void autowireByName(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw,
			MutablePropertyValues pvs) {
		
		// 过滤出bean class中未在XML中配置的非简单类型的属性名
		// 简单类型：八大基础类型、String、Number、枚举、CharSequence、Date、URI、URL、Class、Locale
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			// 如果包含该属性的定义
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				registerDependentBean(propertyName, beanName);
			}
			// 如果不包含属性定义，则略过
		}
	}
	
	/**
	 * 得到bean class的所有有getter或setter方法的非简单类型的属性名称
	 * @param mbd bean定义
	 * @param bw bean实例包装器
	 * @return 属性名称数组
	 */
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<String>();
		PropertyValues pvs = mbd.getPropertyValues();
		
		// 拿到bean class中所有的属性
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
					!BeanUtils.isSimpleProperty(pd.getPropertyType())) {
				result.add(pd.getName());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public <T> Map<String, T> getBeansByType(Class<T> type) {
		return getBeansByType(type, true, true);
	}

	@Override
	public <T> Map<String, T> getBeansByType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) {
		
		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = new LinkedHashMap<String, T>();
		
		for (String beanName : beanNames) {
			result.put(beanName, getBean(beanName, type));
		}
		return result;
	}
	
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				this.ignoreDependencyTypes.contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInterface(pd, this.ignoreDependencyInterfaces);
	}
	
	protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			if (mbd.hasBeanClass() && hasInstantiationAwareBeanPostProcessors()) {
				bean = applyBeanPostProcessorsBeforeInstantiation(mbd.getBeanClass(), beanName);
				if (bean != null) {
					bean = applyBeanPostProcessorAfterInstantiation(bean, beanName);
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}
	
	public Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
				Object bean = ibp.postProcessBeforeInstantiation(beanClass, beanName);
				if (bean != null) {
					return bean;
				}
			}
		}
		return null;
	}
	
	public Object applyBeanPostProcessorAfterInstantiation(Object existingBean, String beanName) {
		Object result = existingBean;
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			result = bp.postProcessAfterInitialization(existingBean, beanName);
			if (result == null) {
				return result;
			}
		}
		return result;
	}
	
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
		if (bean != null && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
					if (exposedObject == null) {
						return exposedObject;
					}
				}
			}
		}
		return exposedObject;
	}
	
	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
		Class<?> beanClass = resolveBeanClass(mbd, beanName);
		
		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(beanName, "非public");
		}
		
		// 在属性上配置factorybean，必须是有factory-method，可以没有factory-bean
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}
		
		// BeanDefinition已经被处理过（被别的bean标签当成factoryMethod）
		if (mbd.resolvedConstructorOrFactoryMethod != null && args == null) {
			if (mbd.constructorArgumentsResolved) {
				return autowireConstructor(beanName, mbd, null, null);
			} else {
				return instantiateBean(beanName, mbd);
			}
		}
		
		// 查看是否有后置处理器进行扩展
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null ||
				mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !(args == null || args.length == 0)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}
		
		return instantiateBean(beanName, mbd);
	}
	
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] args) {
		
		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, args);
	}
	
	/**
	 * 反射生成bean实例，放入BeanWrapper
	 * @param beanName
	 * @param mbd
	 * @return
	 */
	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
		try {
			final BeanFactory parent = this;

			Object beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			
			// 添加一些自定义属性编辑器
			initBeanWrapper(bw);
			return bw;
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "实例化失败", e);
		}
	}
	
	/**
	 * 后置处理器是否存在扩展的构造器生成
	 * @param beanClass 类
	 * @param beanName 类名
	 * @return 构造器
	 */
	protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName) {
		if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					Constructor<?>[] ctors = ((SmartInstantiationAwareBeanPostProcessor) bp).determineCandidateConstructors(beanClass, beanName);
					if (ctors != null) {
						return ctors;
					}
				}
			}
		}
		return null;
	}
	
	public Object applyBeanPostProcessorsBeforeInitialization(String beanName, Object existingBean) {
		Object result = existingBean;
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			result = bp.postProcessBeforeInitialization(result, beanName);
			if (result == null) {
				return result;
			}
		}
		return result;
	}
	
	public Object applyBeanPostProcessorsAfterInitialization(String beanName, Object existingBean) {
		Object result = existingBean;
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			result = bp.postProcessAfterInitialization(result, beanName);
			if (result == null) {
				return result;
			}
		}
		return result;
	}
	
	public Object initializeBean(Object existingBean, String beanName) {
		return initializeBean(beanName, existingBean, null);
	}
	
	private BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {
		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
	}
	
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return parameterNameDiscoverer;
	}

	public void setParameterNameDiscoverer(
			ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}
	
	public InstantiationStrategy getInstantiationStrategy() {
		return instantiationStrategy;
	}

	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	public void setAllowRawInjectionDespiteWrapping(
			boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}

	public void ignoreDependencyType(Class<?> type) {
		this.ignoreDependencyTypes.add(type);
	}

	public void ignoreDependencyInterface(Class<?> type) {
		this.ignoreDependencyInterfaces.add(type);
	}
	
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return autowireCandidateResolver;
	}

	public void setAutowireCandidateResolver(
			final AutowireCandidateResolver autowireCandidateResolver) {
		((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	private Object convertedForProperty(Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {
		if (converter instanceof BeanWrapperImpl) {
			return ((BeanWrapperImpl) converter).convertForProperty(value, propertyName);
		}
		PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
		MethodParameter methodPara = BeanUtils.getWriterMethodParameter(pd);
		return converter.convertIfNecessary(value, methodPara.getParameterType(), methodPara);
	}
	
	private void invokeAwareMethod(final String beanName, final Object bean) {
//		if (bean instanceof BeanNameAware) {
//			
//		}
	}
	
	private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {
		
		private static final long serialVersionUID = 1L;

		public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
			super(methodParameter, false, eager);
		}
	}
	
	private class DependencyObjectFactory implements ObjectFactory<Object>, Serializable {

		private static final long serialVersionUID = 1L;

		private final DependencyDescriptor descriptor;
		
		private final String beanName;
		
		private final Class<?> type;
		
		public DependencyObjectFactory(DependencyDescriptor descriptor, String beanName) {
			this.descriptor = descriptor;
			this.beanName = beanName;
			this.type = determineObjectFactoryType();
		}
		
		public Class<?> determineObjectFactoryType() {
			Type type = this.descriptor.getGenericDependencyType();
			if (type instanceof ParameterizedType) {
				Type arg = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (arg instanceof Class) {
					return (Class<?>) arg;
				}
			}
			return Object.class;
		}
		
		@Override
		public Object getObject() throws BeansException {
			return doResolveDependency(this.descriptor, this.type, this.beanName, null, null);
		}
		
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> results = new ArrayList<String>();
		
		// 检查bean definition
		String[] beanDefinitionNames = getBeanDefinitionNames();
		for (String beanName : beanDefinitionNames) {
			try {
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				
				// 检查是否实例化完成
				if (allowEagerInit || ((mbd.hasBeanClass() || this.allowEagerClassLoading) && 
						!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
					
					// 如果是FactoryBean，则校验由FactoryBean创建的对象是否是匹配的
					boolean isFactoryBean = isFactoryBean(beanName, mbd);
					boolean matchFound = (allowEagerInit || !isFactoryBean || containsSingleton(beanName)) &&
							(includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type);
					if (!matchFound && isFactoryBean) {
						// 如果是FactoryBean，再查看FactoryBean本身是否匹配
						beanName = "&" + beanName;
						matchFound = (includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type);
					}
					
					if (matchFound) {
						results.add(beanName);
					}
				}
			} catch (RuntimeException ex) {
				if (allowEagerInit) {
					throw ex;
				}
				onSuppressedException(ex);
			}
		}
		
		// 检查已经实例化的单例，获取没有bean definition的
		String[] singletonNames = getSingletonNames();
		for (String beanName : singletonNames) {
			if (!containsBeanDefinition(beanName)) {
				// 如果是FactoryBean，则校验由FactoryBean创建的对象是否是匹配的
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						results.add(beanName);
						// 找到匹配的值，则不需要再去校验FactoryBean本身了
						continue;
					}
					// 如果是FactoryBean，再查看FactoryBean本身是否匹配
					beanName = "&" + beanName;
				}
				if (isTypeMatch(beanName, type)) {
					results.add(beanName);
				}
			}
		}
		return results.toArray(new String[results.size()]);
	}

	@Override
	public String[] getBeanDefinitionNames() {
		synchronized (this.beanDefinitionNames) {
			return this.beanDefinitionNames.toArray(new String[this.beanDefinitionNames.size()]);
		}
	}
	
	private boolean requiresEagerInitForType(String factoryBeanName) {
		// 是FactoryBean、并且没有实例化完成
		return factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName);
	}

	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
	}

	@Override
	public void preInstantiateSingletons() {
		LoggerUtil.info("pre-instantiate");
		
		synchronized (this.beanDefinitionMap) {
			for (String beanName : this.beanDefinitionNames) {
				RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
				if (bd.isSingleton()) {
					if (isFactoryBean(beanName)) {
						final FactoryBean<?> factory = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
						boolean isEagerInit = factory instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factory).isEagerInit();
						
						if (isEagerInit) {
							getBean(beanName);
						}
					}
					else {
						getBean(beanName);
					}
				}
			}
		}
	}

	@Override
	public String[] getDependenciesForBean(String beanName) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		RootBeanDefinition mbd = new RootBeanDefinition(beanClass);
		return (T) createBean(beanClass.getName(), mbd, null);
	}

}
