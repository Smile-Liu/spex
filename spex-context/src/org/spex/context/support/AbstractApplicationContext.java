package org.spex.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spex.beans.BeansException;
import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.DisposableBean;
import org.spex.beans.factory.ListableBeanFactory;
import org.spex.beans.factory.config.BeanFactoryPostProcessor;
import org.spex.beans.factory.config.BeanPostProcessor;
import org.spex.beans.factory.support.BeanDefinitionRegistry;
import org.spex.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.spex.context.ApplicationContext;
import org.spex.context.ApplicationEvent;
import org.spex.context.ApplicationEventPublisher;
import org.spex.context.ApplicationListener;
import org.spex.context.ConfigurableApplicationContext;
import org.spex.context.LifecycleProcessor;
import org.spex.context.event.ApplicationEventMulticaster;
import org.spex.context.event.ContextClosedEvent;
import org.spex.context.event.ContextRefreshedEvent;
import org.spex.context.event.ContextStartedEvent;
import org.spex.context.event.ContextStoppedEvent;
import org.spex.context.event.SimpleApplicationEventMulticaster;
import org.spex.core.OrderComparator;
import org.spex.core.Ordered;
import org.spex.core.PriorityOrdered;
import org.spex.core.convert.ConversionService;
import org.spex.util.ClassUtils;
import org.spex.util.LoggerUtil;
import org.spex.util.ObjectUtils;

public abstract class AbstractApplicationContext implements ConfigurableApplicationContext, DisposableBean {

	private static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";
	
	private static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";
	
	private String id = ObjectUtils.identityToString(this);
	
	private String displayName = ObjectUtils.identityToString(this);
	
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = 
		new ArrayList<BeanFactoryPostProcessor>();
	
	private LifecycleProcessor lifecycleProcessor;
	
	private ApplicationEventMulticaster applicationEventMulticaster;
	
	private Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<ApplicationListener<?>>();
	
	private long startupDate;
	
	/** ���� active ��Synchronized����- ������ */
	private final Object activeMonitor = new Object();
	
	private boolean active = false;
	
	private boolean closed = false;
	
	private Thread shutdownHook;
	
	/** ���� refresh��destory ��Synchronized����- ������ */
	private final Object startupShutdownMonitor = new Object();
	
	public AbstractApplicationContext() {
		this(null);
	}
	
	public AbstractApplicationContext(ApplicationContext parent) {
		
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getStartupDate() {
		return startupDate;
	}

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		this.beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		} else {
			this.applicationListeners.add(listener);
		}
	}

	public Set<ApplicationListener<?>> getApplicationListeners() {
		return applicationListeners;
	}

	public ApplicationEventMulticaster getApplicationEventMulticaster() {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticasterû�г�ʼ��");
		}
		return applicationEventMulticaster;
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type,
			boolean includeNonSingletons, boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public Object getBean(String beanName) {
		return getBeanFactory().getBean(beanName);
	}

	@Override
	public boolean containsBean(String beanName) {
		return getBeanFactory().containsBean(beanName);
	}

	@Override
	public Class<?> getType(String name) {
		return getBeanFactory().getType(name);
	}

	/**
	 * web���
	 * 	1.ContextLoaderListener��ʵ�ʷ�����ContextLoader��createWebApplicationContext������
	 * 	2.
	 * @see org.spex.context.ConfigurableApplicationContext#refresh()
	 */
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// 1.����flag��ʶ��ʼ��
			prepareRefresh();
			
			// 2.��ȡBeanFactory
			// ����Ǵ�ContextLoaderListener���ģ���ʹ��ȫ�ֵ�contextConfigLocation��Ϊ�����ļ�
			// �����
			// 	���ȣ�new DefaultListableBeanFactory
			//  ��Σ�ע��LocalVariableTableParameterNameDiscoverer��QualifierAnnotationAutowireCandidateResolver��Autowired��Qualifierע�⣩
			//  Ȼ��new XmlBeanDefinitionReader����ȡ�����ļ����ݣ�����BeanDefinition
			//  ������ɵ�BeanDefinition����beanFactory
			ListableBeanFactory beanFactory = obtainFreshBeanFactory();
			
			// 3.ע��SystemProperties��SystemEnvironment�����BeanClassLoader��ע��ResolvableDependency
			prepareBeanFactory(beanFactory);
			
			try {
				
				// 4.ע��ServletContext��ServletConfig
				postProcessBeanFactory(beanFactory);
				
				// 5.ִ��BeanFactoryPostProcessor
				invokeBeanFactoryPostProcessors(beanFactory);
				
				// 6.ע��BeanPostProcessor��BeanFactory
				registerBeanPostProcessors(beanFactory);
				
				// 7.��ʼ��ȫ����Դ��ע��messageSource��
				initMessageSource();
				
				// 8.ע��applicationEventMulticaster
				initApplicationEventMulticaster();
				
				// 9.�յ�
				onRefresh();
				
				// 10.ע��ApplicationListener
				registerListener();
				
				// 11.���ʵ��������ǰʵ�������еĵ���bean��ָ����ǰʵ������FactoryBean
				finishBeanFactoryInitialization(beanFactory);
				
				// 12.������������ִ���������ˢ��
				finishRefresh();
				
			} catch (BeansException e) {
				
				destoryBeans();
				
				cancelRefresh();
				
				throw e;
			}
		}
	}
	
	@Override
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			
			if (this.shutdownHook != null) {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
		}
	}

	protected void onClose() {
		
	}
	
	@Override
	public boolean isActive() {
		synchronized (this.activeMonitor) {
			return this.active;
		}
	}

	@Override
	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}

	@Override
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}

	@Override
	public boolean isRunning() {
		return getLifecycleProcessor().isRunning();
	}

	@Override
	public void destroy() throws Exception {
		close();
	}

	@Override
	public ListableBeanFactory getListableBeanFactory() {
		return getBeanFactory();
	}
	
	@Override
	public void publishEvent(ApplicationEvent event) {
		getApplicationEventMulticaster().multicastEvent(event);
	}

	public LifecycleProcessor getLifecycleProcessor() {
		if (this.lifecycleProcessor == null) {
			throw new IllegalStateException("LifecycleProcessorû�г�ʼ��");
		}
		return lifecycleProcessor;
	}

	protected void initLifecycleProcessor() {
		ListableBeanFactory beanFactory = getBeanFactory();
		
		if (beanFactory.containsBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
		} else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, defaultProcessor);
		}
	}
	
	public void prepareRefresh() {
		this.startupDate = System.currentTimeMillis();
		
		synchronized (this.activeMonitor) {
			this.active = true;
		}
		
		LoggerUtil.info("��ʼ refresh");
	}
	
	protected void cancelRefresh() {
		synchronized (this.activeMonitor) {
			this.active = false;
		}
	}
	
	protected void doClose() {
		boolean actuallyClose;
		
		synchronized (this.activeMonitor) {
			actuallyClose = this.active && !this.closed;
			this.closed = true;
		}
		
		if (actuallyClose) {
			LoggerUtil.info("���ڹر�");
		}
		
		publishEvent(new ContextClosedEvent(this));
		
		getLifecycleProcessor().onClose();
		
		destoryBeans();
		
		closeBeanFactory();
		
		onClose();
		
		synchronized (this.activeMonitor) {
			this.active = false;
		}
	}
	
	public ListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
		ListableBeanFactory beanFactory = getBeanFactory();
		
		return beanFactory;
	}
	
	@SuppressWarnings("rawtypes")
	protected void prepareBeanFactory(ListableBeanFactory beanFactory) {
		// ����ClassLoader
		beanFactory.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
		
		// ����Զ�ע��ApplicationConext�ĺ��ô�����
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
		
		// ע��
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);
		
		// ע��systemProperties��systemEnvironment������bean
		if (!beanFactory.containsBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			Map systemProperties = System.getProperties();
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, systemProperties);
		}
		if (!beanFactory.containsBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			Map systemEnvironment = System.getenv();
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, systemEnvironment);
		}
	}
	
	protected void postProcessBeanFactory(ListableBeanFactory beanFactory) {
	}
	
	protected void invokeBeanFactoryPostProcessors(ListableBeanFactory beanFactory) {
		// ��һ����ִ��BeanDefinitionRegistryPostProcessors
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			
			// ��ȡ��ӵ�ApplicationContext��beanFactoryPostProcessors
			for (BeanFactoryPostProcessor beanFactoryPostProcessor : getBeanFactoryPostProcessors()) {
				if (beanFactoryPostProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					((BeanDefinitionRegistryPostProcessor) beanFactoryPostProcessor).postProcessBeanDefinitionRegistry(registry);
				}
			}
			
			// ��ȡע�ᵽ������BeanDefinitionRegistryPostProcessor
			Collection<BeanDefinitionRegistryPostProcessor> registryPostProcessors = 
				beanFactory.getBeansByType(BeanDefinitionRegistryPostProcessor.class, true, false).values();
			for (BeanDefinitionRegistryPostProcessor postProcessor : registryPostProcessors) {
				postProcessor.postProcessBeanDefinitionRegistry(registry);
			}
		}
		
		// �ڶ�����ִ��BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(getBeanFactoryPostProcessors(), beanFactory);
		
		// ����������ȡ�����е�BeanFactoryPostProcessor������Ҫ��ʼ����bean
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
		
		// ���Ƿ�ʵ�� PriorityOrdered, Ordered, �������ֱ���
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		
		for (String ppName : postProcessorNames) {
			if (isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				orderedPostProcessorNames.add(ppName);
			}
		}
		
		// ���ȣ�ִ��PriorityOrdered
		OrderComparator.sort(priorityOrderedPostProcessors);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);
		
		// ���ţ�ʵ����orderedPostProcessorNames��bean��ִ��
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		OrderComparator.sort(orderedPostProcessors);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);
		
		// ���ִ��������
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
	}
	
	public void destoryBeans() {
		// ��ʵ��
	}
	
	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}
	
	protected void registerBeanPostProcessors(ListableBeanFactory beanFactory) {
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
		
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
//		List<BeanPostProcessor> internalPostProcessors = new ArrayList<BeanPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		
		for (String ppName : postProcessorNames) {
			if (isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanPostProcessor.class));
			}
			else if (isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}
		
		// ���ȣ�ע��PriorityOrdered
		OrderComparator.sort(priorityOrderedPostProcessors);
		registerBeanPostProcessors(priorityOrderedPostProcessors, beanFactory);
		
		// ���ţ�ע��Ordered
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(ppName, BeanPostProcessor.class));
		}
		OrderComparator.sort(orderedPostProcessors);
		registerBeanPostProcessors(orderedPostProcessors, beanFactory);
		
		// ���ע������
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanPostProcessor.class));
		}
		registerBeanPostProcessors(nonOrderedPostProcessors, beanFactory);
	}
	
	protected void initMessageSource() {
		
	}
	
	protected void initApplicationEventMulticaster() {
		ListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster = 
				beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
		} else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
		}
	}
	
	protected void onRefresh() {
		
	}
	
	protected void registerListener() {
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}
	}
	
	protected void finishBeanFactoryInitialization(ListableBeanFactory beanFactory) {
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}
		
		beanFactory.freezeConfiguration();
		
		// ��ǰʵ�������еĵ���bean��ָ����ǰʵ������FactoryBean
		beanFactory.preInstantiateSingletons();
	}
	
	protected void finishRefresh() {
		initLifecycleProcessor();
		
		getLifecycleProcessor().onRefresh();
		
		publishEvent(new ContextRefreshedEvent(this));
	}
	
	public boolean isTypeMatch(String name, Class<?> targetType) {
		return getBeanFactory().isTypeMatch(name, targetType);
	}

	private void invokeBeanFactoryPostProcessors(List<BeanFactoryPostProcessor> postProcessors, ListableBeanFactory beanFactory) {
		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}
	
	private void registerBeanPostProcessors(List<BeanPostProcessor> postProcessors, ListableBeanFactory beanFactory) {
		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}
	

	@Override
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		
	}

	@Override
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		return getBeanFactory().createBean(beanClass);
	}
	
	@Override
	public <T> T getBean(String beanName, Class<T> requiredType) {
		return getBeanFactory().getBean(beanName, requiredType);
	}

	@Override
	public <T> T getBean(Class<T> beanClass) {
		return getBeanFactory().getBean(beanClass);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		getBeanFactory().setBeanClassLoader(classLoader);
	}

	@Override
	public void registerResolvableDependency(Class<?> dependencyType,
			Object autowiredValue) {
		getBeanFactory().registerResolvableDependency(dependencyType, autowiredValue);
	}

	@Override
	public <T> Map<String, T> getBeansByType(Class<T> type) {
		return getBeanFactory().getBeansByType(type);
	}

	@Override
	public <T> Map<String, T> getBeansByType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) {
		return getBeanFactory().getBeansByType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public void setConversionService(ConversionService conversionService) {
		getBeanFactory().setConversionService(conversionService);
	}

	@Override
	public ConversionService getConversionService() {
		return getBeanFactory().getConversionService();
	}

	@Override
	public void freezeConfiguration() {
		getBeanFactory().freezeConfiguration();
	}

	@Override
	public void preInstantiateSingletons() {
		getBeanFactory().preInstantiateSingletons();
	}

	@Override
	public boolean isFactoryBean(String beanName) {
		return getBeanFactory().isFactoryBean(beanName);
	}

	@Override
	public String[] getDependenciesForBean(String beanName) {
		return getBeanFactory().getDependenciesForBean(beanName);
	}

	@Override
	public void registerSingleton(String beanName, Object singletonObject) {
		getBeanFactory().registerSingleton(beanName, singletonObject);
	}

	@Override
	public Object getSingleton(String beanName) {
		return getBeanFactory().getSingleton(beanName);
	}

	@Override
	public boolean containsSingleton(String beanName) {
		return getBeanFactory().containsSingleton(beanName);
	}

	@Override
	public String[] getSingletonNames() {
		return getBeanFactory().getSingletonNames();
	}
	
	protected abstract ListableBeanFactory getBeanFactory() throws IllegalStateException;
	
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;
	
	protected abstract void closeBeanFactory();
}
