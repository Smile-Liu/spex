package org.spex.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.DisposableBean;
import org.spex.beans.factory.ObjectFactory;
import org.spex.beans.factory.config.SingletonBeanRegistry;
import org.spex.util.StringUtils;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

	/** ��������Ļ��棬 bean name --> bean instance */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>();
	
	/** ������������Ļ��棬bean name --> ObjectFactory */
	private final Map<String, ObjectFactory<Object>> singletonFactories = new HashMap<String, ObjectFactory<Object>>();
	
	/** ���ڵĵ�������bean name --> bean instance */
	private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>();
	
	/** �Ѿ�ע����ɵĵ����������Ƽ��� */
	private final Set<String> registeredSingletons = new LinkedHashSet<String>();
	
	/** ��¼���ڴ����ĵ������� */
	private final Set<String> singletonsCurrentlyInCreation = Collections.synchronizedSet(new HashSet<String>());
	
	/** ��¼������ϵ��bean name -> ����bean name�� */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<String, Set<String>>();

	/** ��¼������ϵ��bean name -> bean name������ */
	private final Map<String, Set<String>> dependenciesBeanMap = new ConcurrentHashMap<String, Set<String>>();
	
	/** ���ӹ�ϵ��bean */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<String, Set<String>>();
	
	/** ��¼�����ٵ�Bean */
	private final Map<String, Object> disposableBeans = new LinkedHashMap<String, Object>();
	
	/** ����Ƿ��������ٵ�ǰ�� */
	private boolean singletonsCurrentlyInDestruction = false;
	
	/** �쳣�ļ��� */
	private Set<Exception> suppressedExceptions;
	
	public void registerSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("������ӵ���Bean����Ϊ�Ѿ�������Ϊ" + beanName + "�ĵ�����");
			}
			addSingleton(beanName, singletonObject);
		}
	}
	
	
	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}
	
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null) {
			synchronized (this.singletonObjects) {
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
					ObjectFactory<Object> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		
		return singletonObject == null ? null : singletonObject;
	}
	
	protected Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		if (!StringUtils.hasText(beanName)) {
			throw new IllegalArgumentException("'beanName'����Ϊ��");
		}
		
		synchronized (this.singletonObjects) {
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationException("��ǰBeanFactory�������٣����ܴ���ʵ��");
				}
				beforeSingletonCreation(beanName);
				
				boolean recordSuppressedExceptions = this.suppressedExceptions == null;
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<Exception>();
				}
				
				try {
					singletonObject = singletonFactory.getObject();
				} catch (BeanCreationException e) {
					for (Exception suppressedException : this.suppressedExceptions) {
						e.addRelatedCauses(suppressedException);
					}
					throw e;
				} finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					afterSingletonCreation(beanName);
				}
				
				addSingleton(beanName, singletonObject);
			}
			return singletonObject;
		}
	}
	
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized(this.singletonObjects) {
			this.singletonObjects.put(beanName, singletonObject);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.add(beanName);
		}
	}
	
	protected void beforeSingletonCreation(String beanName) {
		if (!this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCreationException(beanName, "�Ѿ����ڴ����еĸö�����");
		}
	}
	
	protected void afterSingletonCreation(String beanName) {
		if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new BeanCreationException(beanName, "û�����ڴ����ö���");
		}
	}
	
	protected void registerDependentBean(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
			if (dependentBeans == null) {
				dependentBeans = new LinkedHashSet<String>();
				this.dependentBeanMap.put(beanName, dependentBeans);
			}
			dependentBeans.add(dependentBeanName);
		}
		synchronized (this.dependenciesBeanMap) {
			Set<String> dependenciesBeans = this.dependenciesBeanMap.get(dependentBeanName);
			if (dependenciesBeans == null) {
				dependenciesBeans = new LinkedHashSet<String>();
				this.dependenciesBeanMap.put(dependentBeanName, dependenciesBeans);
			}
			dependenciesBeans.add(beanName);
		}
	}
	
	public void registerContainedBean(String containedBeanName, String containingBeanName) {
		synchronized (this.containedBeanMap) {
			Set<String> containedBeans = this.containedBeanMap.get(containingBeanName);
			if (containedBeans == null) {
				containedBeans = new LinkedHashSet<String>();
				this.containedBeanMap.put(containingBeanName, containedBeans);
			}
			containedBeans.add(containedBeanName);
		}
	}
	
	protected void onSuppressedException(Exception ex) {
		synchronized (this.singletonObjects) {
			if (this.suppressedExceptions != null) {
				this.suppressedExceptions.add(ex);
			}
		}
	}
	
	public boolean containsSingleton(String beanName) {
		return this.singletonObjects.containsKey(beanName);
	}
	
	public boolean hasDependentBeans(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}
	
	public final boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}
	
	public String[] getSingletonNames() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.toArray(new String[this.registeredSingletons.size()]);
		}
	}
	protected void addSingletonFactory(String beanName, ObjectFactory<Object> singletonFactory) {
		if (singletonFactory == null) {
			throw new IllegalArgumentException("Singleton Factory����Ϊ��");
		}
		synchronized (this.singletonObjects) {
			if (!this.singletonObjects.containsKey(beanName)) {
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
				this.registeredSingletons.add(beanName);
			}
		}
	}
	
	protected boolean hasDependentBean(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}
	
	public String[] getDependentBeans(String beanName) {
		Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
		if (dependentBeans == null) {
			dependentBeans = new HashSet<String>();
		}
		return dependentBeans.toArray(new String[dependentBeans.size()]);
	}
	
	protected void removeSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.remove(beanName);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.remove(beanName);
		}
	}
	
	protected void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}
}
