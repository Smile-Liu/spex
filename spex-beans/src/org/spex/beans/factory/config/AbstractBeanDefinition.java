package org.spex.beans.factory.config;

import org.spex.beans.factory.MutablePropertyValues;

public abstract class AbstractBeanDefinition implements BeanDefinition {

	public static final int AUTOWIRE_NO = 0;
	public static final int AUTOWIRE_BY_NAME = 1;
	public static final int AUTOWIRE_BY_TYPE = 2;
	public static final int AUTOWIRE_CONSTRUCTOR = 3;
	
	private static final String SCOPE_SINGLETON = "singleton";
	
	private boolean lenientConstructorSolution = true;
	
	private Class<?> beanClass;
	private String beanClassName;
	private String scope;
	private String[] dependsOn;
	
	/**
	 * factory-bean和factory-method
	 * 如果工厂方法是静态方法，则只需要使用factory-method即可
	 * 如果工厂方法是实例方法，则需要两者一起使用
	 */
	private String factoryBeanName;
	private String factoryMethodName;
	
	private String initMethodName;
	private boolean primary = false;
	private String destroyMethodName;
	
	private int autowireMode = AUTOWIRE_NO;
	
	private ConstructorArgumentValues constructorArgumentValues;
	
	private MutablePropertyValues propertyValues;
	
	private boolean nonPublicAccessAllowed = true;
	
	private boolean autowireCandidate = true;
	
	public AbstractBeanDefinition() {
		setConstructorArgumentValues(null);
		setPropertyValues(null);
	}
	
	public AbstractBeanDefinition(BeanDefinition original) {
		setBeanClassName(original.getBeanClassName());
		setFactoryBeanName(original.getFactoryBeanName());
		setPropertyValues(original.getPropertyValues());
		
		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			
			setAutowireMode(originalAbd.getAutowireMode());
			setBeanClass(originalAbd.getBeanClass());
			setConstructorArgumentValues(originalAbd.getConstructorArgumentValues());
			setInitMethodName(originalAbd.getInitMethodName());
			setPrimary(originalAbd.isPrimary());
			setScope(originalAbd.getScope());
			setDependsOn(originalAbd.getDependsOn());
			setFactoryMethodName(originalAbd.getFactoryMethodName());
		}
	}
	
	public void validate() {
		
	}

	public boolean isSingleton() {
		return SCOPE_SINGLETON.equals(this.scope);
	}


	@Override
	public String getBeanClassName() {
		return this.beanClassName;
	}

	@Override
	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}

	@Override
	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	@Override
	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
	
	public boolean hasBeanClass() {
		return this.beanClass != null;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public int getAutowireMode() {
		return autowireMode;
	}

	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String[] getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String getInitMethodName() {
		return initMethodName;
	}

	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}
	
	public boolean hasConstructorArgumentValues() {
		return !this.constructorArgumentValues.isEmpty();
	}

	public String getDestroyMethodName() {
		return destroyMethodName;
	}

	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	public ConstructorArgumentValues getConstructorArgumentValues() {
		return constructorArgumentValues;
	}

	public void setConstructorArgumentValues(
			ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues = constructorArgumentValues == null ? new ConstructorArgumentValues() : constructorArgumentValues;
	}

	public boolean isAutowireCandidate() {
		return autowireCandidate;
	}

	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}

	@Override
	public MutablePropertyValues getPropertyValues() {
		return propertyValues;
	}

	@Override
	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = propertyValues == null ? new MutablePropertyValues() : propertyValues;
	}

	public boolean isNonPublicAccessAllowed() {
		return nonPublicAccessAllowed;
	}

	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}

	public String getFactoryMethodName() {
		return factoryMethodName;
	}

	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	public boolean isLenientConstructorSolution() {
		return lenientConstructorSolution;
	}

	public void setLenientConstructorSolution(boolean lenientConstructorSolution) {
		this.lenientConstructorSolution = lenientConstructorSolution;
	}
	
}
