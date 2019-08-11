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
	
	/** bean definition names �б� */
	private final List<String> beanDefinitionNames = new ArrayList<String>();
	
	private ParameterNameDiscoverer parameterNameDiscoverer;
	
	private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();
	
	/** �����Զ�����ѭ������ */
	private boolean allowCircularReference = true;
	
	/** �����ʼ״̬����������ٰ�װ */
	private boolean allowRawInjectionDespiteWrapping = false;
	
	/** ���� */
	private final Set<Class<?>> ignoreDependencyTypes = new HashSet<Class<?>>();
	private final Set<Class<?>> ignoreDependencyInterfaces = new HashSet<Class<?>>();
	
	private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();
	
	private boolean allowEagerClassLoading = true;
	
	/** �����������͵���Ӧ���Զ�װ���ֵ */
	private final Map<Class<?>, Object> resolvableDependencies = new HashMap<Class<?>, Object>();
	
	private boolean configurationFrozen = false;
	
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
		if (!StringUtils.hasText(beanName)) {
			throw new IllegalArgumentException("Bean Name ����Ϊ��");
		}
		if (beanDefinition == null) {
			throw new IllegalArgumentException("Bean Definition ����Ϊ��");
		}
		
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			} catch (BeanDefinitionValidationException e) {
				throw new BeanDefinitionStoreException("Bean Definition��֤ʧ��", e, beanName);
			}
		}
		
		synchronized (this.beanDefinitionMap) {
			if (this.beanDefinitionMap.containsKey(beanName)) {
				throw new BeanDefinitionStoreException("ע��Bean Definition " + beanName + "ʧ�ܣ���Ϊ�Ѿ�������");
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
		
		// ��֧�� lookupMethod��replacedMethod������û��methodOverrides
		
		try {
			// ִ��һ�� BeanPostProcessor
			Object bean = resolveBeforeInstantiation(beanName, mbd);
			
			// �����ʵ����ǰ��ִ�е�BeanPostProcessor�н�������жϲ����ؽ��
			if (bean != null) {
				return bean;
			}
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "BeanPostProcessor��beanʵ����ǰ before instantiation ִ��ʧ��", e);
		}
		
		// ��������ʵ����
		Object beanInstance = doCreateBean(beanName, mbd, args);
		return beanInstance;
	}

	protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
		BeanWrapper instanceWrapper = createBeanInstance(beanName, mbd, args);
		
		final Object bean = instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null;
//		Class<?> beanType = instanceWrapper != null ? instanceWrapper.getWrappedClass() : null;
		
		// ��֧�� MergedBeanDefinitionPostProcessor��BeanPostProcessor
		
		// ���ѭ������
		// bean���ڱ����������������Զ�����������ڴ�����bean����ObjectFactory����¶��ȥ
		// ��������bean��Beanʵ����ʱ����getSingleton()������ֱ�Ӵ�singletonFactory������ȡ�����ڴ�����bean������
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
		
		// ������Ե�BeanWrapper
		populateBean(beanName, mbd, instanceWrapper);
		
		// ��ʼ��
		if (exposedObject != null) {
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		
		// У���Ƿ���������bean�ĳ�ʼ�汾����bean���ձ���װ�������°汾��һ��
		if (earlySingletonExposure) {
			// ���reference����ǰ�汩¶��ȥ��bean����������ͳ�ʼ��ǰ��exposedObject��һ����
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					// ���հ汾�ͳ�ʼ�汾һ�£���û������
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
								"bean '" + beanName + "'�ĳ�ʼ�汾������bean�����������ո�bean����װ�ˡ�" +
								"Ҳ����˵������bean����Ҫ���հ汾�����bean���⾭������Ϊ����Ľ�������ƥ���ˡ�" +
								"���������� 'getBeanNamesOfType'ִ��ʱ��'allowEagerInit'������Ϊfalse");
					}
				}
			}
		}
		
		// ע��ע��
		registerDisposableBeanIfNecessary(beanName, bean, mbd);
		
		return exposedObject;
	}
	
	
	protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
		// ��Բ�ͬ���͵�Awareע���Ӧ��ֵ
		// BeanNameAware <- beanName
		// BeanClassLoaderAware <- classLoader
		// BeanFactoryAware <- AbstractAutowireCapableBeanFactory.class
		invokeAwareMethod(beanName, bean);
		
		Object wrappedBean = bean;
		
		// ���ú��ô�������before initialization
		if (mbd == null) {
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(beanName, wrappedBean);
		}
		
		// ʵ�ֳ�ʼ�������������֣�ʵ����InitializingBean�ӿڡ�������init-method
		try {
			invokeInitMethods(beanName, wrappedBean, mbd);
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "����init-methodʧ��", e);
		}
		
		// ���ú��ô�������after initialization
		if (mbd == null) {
			wrappedBean = applyBeanPostProcessorsAfterInitialization(beanName, wrappedBean);
		}
		
		return wrappedBean;
	}
	
	/**
	 * ʵ�ֳ�ʼ��
	 * 1.ʵ����InitializingBean�ӿڵĵ���afterPropertiesSet()
	 * 2.������init-method�Ľ��е���ָ���ķ���
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

		// У��init-method�Ƿ����
		final Method initMethod = (mbd.isNonPublicAccessAllowed() ? 
				BeanUtils.findMethod(bean.getClass(), initMethodName) :
				ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));
		if (initMethod == null) {
			// �����Գ�ʼ��������ǿ��Ҫ��
			return ;
		}
		
		try {
			// ����
			ReflectionUtils.makeAccessible(initMethod);
			initMethod.invoke(bean);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (IllegalAccessException e) {
			throw e;
		}
	}
	
	protected void populateBean(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw) {
		// �����ļ��ж����property�ӱ�ǩ
		PropertyValues pvs = mbd.getPropertyValues();
		
		if (bw == null) {
			if (!pvs.isEmpty()) {
				throw new BeanCreationException(beanName, "���ܰ�property��䵽�յ�beanʵ����");
			} else {
				return;
			}
		}
		
		// ���������ǰ������InstantiationAwareBeanPostProcessorȥ�޸�bean instance��״̬
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
		
		// Ӧ������
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
					throw new BeanCreationException(beanName, "�������ֵʱ��������", e);
				}
			}
			original = mpvs.getPropertyValueList();
		} else {
			original = Arrays.asList(pvs.getPropertyValues());
		}
		
		// ����ת����
		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);
		// ��ԭʼ���ݸ��Ƶ���һ���ط�
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
				
				// ��¼ת�����ֵ����ֹ�ظ�ת��
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
		
		// ���������ҵ�ʵ����Ҫ����û�����õ������
		// Ȼ��������Ե����ͣ��ٸ�������ȥ�������õĶ�Ӧ��beanName
		// Ȼ�����bean name���bean��һ����װ��һ��PropertyValue������pvs
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			
			// ��Զ��Ҫ�Զ�ע������Object����ʹ����һ������ȷ�ķǼ����͵�����
			if (!Object.class.equals(pd.getPropertyType())) {
				// ��ȡ���Ե�setter��������
				MethodParameter methodParam = BeanUtils.getWriterMethodParameter(pd);
				
				boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
				// ����������
				DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
				
				// * ����
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
			// ���ڶ�Ϊ��
		}
		
		if (type.isArray()) {
			// �����������������
			Class<?> componentType = type.getComponentType();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, desc);
			if (matchingBeans.isEmpty()) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(componentType, "������������һ��bean����ƥ�䵽������������δ�ҵ�");
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
					throw new BeansException("û��Ϊ����[" + type.getName() + "]����Ԫ������");
				}
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, desc);
			if (matchingBeans.isEmpty()) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(elementType, "��������Ϊ[" + elementType.getName() + "]" +
							"������������һ��bean����ƥ�䵽������������δ�ҵ�");
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
					throw new BeansException("Map [" + type.getName() + "]�ļ���K������[" + mapKeyType + "ֻ����String");
				}
				return null;
			}
			
			Class<?> mapValueType = desc.getMapValueType();
			if (mapValueType == null) {
				if (desc.isRequired()) {
					throw new BeansException("Map [" + type.getName() + "]��δ����ֵ��V��������");
				}
				return null;
			}
			
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, mapValueType, desc);
			if (matchingBeans == null) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(mapValueType, "������������һ��bean" +
							"��ֵ����Ϊ[" + mapValueType.getName() + "]������ƥ�䵽������������δ�ҵ�");
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		} else {
			// �������ͣ�����һ��ֵ
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, desc);
			if (matchingBeans == null) {
				if (desc.isRequired()) {
					throw new NoSuchBeanDefinitionException(type, "������������һ��bean����ƥ�䵽������������δ�ҵ�");
				}
				return null;
			}
			if (matchingBeans.size() > 1) {
				String primaryBeanName = determinePrimaryCandidate(matchingBeans, desc);
				if (primaryBeanName == null) {
					throw new NoSuchBeanDefinitionException(type, "��������һ��bean����ƥ�䵽��������" +
							"�����ҵ�" + matchingBeans.size() + "����" + matchingBeans.keySet());
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
								"���ں�ѡ����" + candidateBeans.keySet() + "�ҵ����'primary'bean");
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
		
		// �����������Ƿ���FactoryBean
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
			// ��resolvedConstructorOrFactoryMethod��ֵ���������з������ҵ�Ψһƥ���һ��
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		return getAutowireCandidateResolver().isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName), descriptor);
	}
	
	protected void autowireByName(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw,
			MutablePropertyValues pvs) {
		
		// ���˳�bean class��δ��XML�����õķǼ����͵�������
		// �����ͣ��˴�������͡�String��Number��ö�١�CharSequence��Date��URI��URL��Class��Locale
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			// ������������ԵĶ���
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				registerDependentBean(propertyName, beanName);
			}
			// ������������Զ��壬���Թ�
		}
	}
	
	/**
	 * �õ�bean class��������getter��setter�����ķǼ����͵���������
	 * @param mbd bean����
	 * @param bw beanʵ����װ��
	 * @return ������������
	 */
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<String>();
		PropertyValues pvs = mbd.getPropertyValues();
		
		// �õ�bean class�����е�����
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
			throw new BeanCreationException(beanName, "��public");
		}
		
		// ������������factorybean����������factory-method������û��factory-bean
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}
		
		// BeanDefinition�Ѿ���������������bean��ǩ����factoryMethod��
		if (mbd.resolvedConstructorOrFactoryMethod != null && args == null) {
			if (mbd.constructorArgumentsResolved) {
				return autowireConstructor(beanName, mbd, null, null);
			} else {
				return instantiateBean(beanName, mbd);
			}
		}
		
		// �鿴�Ƿ��к��ô�����������չ
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
	 * ��������beanʵ��������BeanWrapper
	 * @param beanName
	 * @param mbd
	 * @return
	 */
	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
		try {
			final BeanFactory parent = this;

			Object beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			
			// ���һЩ�Զ������Ա༭��
			initBeanWrapper(bw);
			return bw;
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "ʵ����ʧ��", e);
		}
	}
	
	/**
	 * ���ô������Ƿ������չ�Ĺ���������
	 * @param beanClass ��
	 * @param beanName ����
	 * @return ������
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
		
		// ���bean definition
		String[] beanDefinitionNames = getBeanDefinitionNames();
		for (String beanName : beanDefinitionNames) {
			try {
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				
				// ����Ƿ�ʵ�������
				if (allowEagerInit || ((mbd.hasBeanClass() || this.allowEagerClassLoading) && 
						!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
					
					// �����FactoryBean����У����FactoryBean�����Ķ����Ƿ���ƥ���
					boolean isFactoryBean = isFactoryBean(beanName, mbd);
					boolean matchFound = (allowEagerInit || !isFactoryBean || containsSingleton(beanName)) &&
							(includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type);
					if (!matchFound && isFactoryBean) {
						// �����FactoryBean���ٲ鿴FactoryBean�����Ƿ�ƥ��
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
		
		// ����Ѿ�ʵ�����ĵ�������ȡû��bean definition��
		String[] singletonNames = getSingletonNames();
		for (String beanName : singletonNames) {
			if (!containsBeanDefinition(beanName)) {
				// �����FactoryBean����У����FactoryBean�����Ķ����Ƿ���ƥ���
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						results.add(beanName);
						// �ҵ�ƥ���ֵ������Ҫ��ȥУ��FactoryBean������
						continue;
					}
					// �����FactoryBean���ٲ鿴FactoryBean�����Ƿ�ƥ��
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
		// ��FactoryBean������û��ʵ�������
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
