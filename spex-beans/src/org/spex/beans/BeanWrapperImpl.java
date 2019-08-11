package org.spex.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spex.beans.factory.MutablePropertyValues;
import org.spex.core.MethodParameter;
import org.spex.util.StringUtils;

public class BeanWrapperImpl extends PropertyEditorRegistrySupport implements BeanWrapper {

	private TypeConverterDelegate typeConverterDelegate;
	
	private CachedIntrospectionResults cachedIntrospectionResults;
	
	private Object object;
	private String nestedPath;
	private Object rootObject;
	
	private Map<String, BeanWrapperImpl> nestedBeanWrappers;
	
	private boolean extractOldValueForEditor = false;
	
	public BeanWrapperImpl() {
		this(true);
	}

	public BeanWrapperImpl(boolean registerDefaultEditors) {
		this.typeConverterDelegate = new TypeConverterDelegate(this);
	}

	public BeanWrapperImpl(Object object) {
		setWrappedInstance(object);
	}
	
	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl superBw) {
		setWrappedInstance(object, nestedPath, superBw.getWrappedInstance());
		setExtractOldValueForEditor(superBw.isExtractOldValueForEditor());
		setConversionService(superBw.getConversionService());
	}
	
	@Override
	public <T> T convertIfNecessary(Object value, Class<?> requiredType, MethodParameter methodParam) {
		return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParam);
	}

	@Override
	public <T> T convertIfNecessary(Object value, Class<?> requiredType) {
		return this.typeConverterDelegate.convertIfNecessary(value, requiredType);
	}

	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return getCachedIntrospectionResults().getPropertyDescriptors();
	}
	
	@Override
	public PropertyDescriptor getPropertyDescriptor(String propertyPath) {
		return getCachedIntrospectionResults().getPropertyDescriptor(propertyPath);
	}
	
	
	public void setWrappedInstance(Object object) {
		setWrappedInstance(object, "", null);
	}
	
	public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
		this.object = object;
		this.nestedPath = nestedPath != null ? nestedPath : "";
		this.rootObject = !"".equals(this.nestedPath) ? rootObject : object;
		this.typeConverterDelegate = new TypeConverterDelegate(this);
	}
	
	public String getNestedPath() {
		return nestedPath;
	}

	public void setNestedPath(String nestedPath) {
		this.nestedPath = nestedPath;
	}

	public Object getRootObject() {
		return rootObject;
	}

	public void setRootObject(Object rootObject) {
		this.rootObject = rootObject;
	}

	public boolean isExtractOldValueForEditor() {
		return extractOldValueForEditor;
	}

	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}

	@Override
	public Object getWrappedInstance() {
		return this.object;
	}

	@Override
	public Class<?> getWrappedClass() {
		return this.object != null ? this.object.getClass() : null;
	}

	private CachedIntrospectionResults getCachedIntrospectionResults() {
		if (this.cachedIntrospectionResults == null) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
		}
		return this.cachedIntrospectionResults;
	}

	@Override
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, false);
	}

	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
			throws BeansException {
		setPropertyValues(pvs, ignoreUnknown, false);
	}

	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown,
			boolean ignoreInvalid) throws BeansException {
		
		List<PropertyAccessException> propertyAccessorExceptions = null;
		List<PropertyValue> propertyValues = pvs instanceof MutablePropertyValues ?
				((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues());
				
		for (PropertyValue pv : propertyValues) {
			try {
				setPropertyValue(pv);
			} catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
			} catch (PropertyAccessException ex) {
				if (propertyAccessorExceptions == null) {
					propertyAccessorExceptions = new ArrayList<PropertyAccessException>();
				}
				propertyAccessorExceptions.add(ex);
			}
		}
		if (propertyAccessorExceptions != null) {
			PropertyAccessException[] paeArr = 
				propertyAccessorExceptions.toArray(new PropertyAccessException[propertyAccessorExceptions.size()]);
			throw new PropertyBatchUpdateException(paeArr);
		}
	}

	protected void setPropertyValue(PropertyValue pv) {
//		setPropertyValue(pv.getName(), pv.getValue());
		PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
		if (tokens == null) {
			String propertyName = pv.getName();
			BeanWrapperImpl nestedBw = getNestedBeanWrapperForPropertyPath(propertyName);
			
			tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
			if (nestedBw == this) {
				pv.getOriginalPropertyValue().resolvedTokens = tokens;
			}
			nestedBw.setPropertyValue(tokens, pv);
		} else {
			setPropertyValue(tokens, pv);
		}
	}
	
	public void setPropertyValue(String propertyName, Object value) {
//		BeanWrapperImpl nestedBw = getNestedBeanWrapperForPropertyPath(propertyName);
//		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
//		nestedBw.setPropertyValue(tokens, new PropertyValue(propertyName, value));
	}
	
	@Override
	public boolean isWritableProperty(String propertyName) {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getWriteMethod() != null) {
					return true;
				}
			} else {
				// 可能是其他类型的属性
				getPropertyValue(propertyName);
				return true;
			}
		} catch (InvalidPropertyException e) {
			// 只有抛异常的时候返回false
		}
		return false;
	}
	
	public final Class<?> getRootClass() {
		return this.rootObject != null ? this.rootObject.getClass() : null;
	}
	
	@Override
	public Object getPropertyValue(String propertyName) {
		BeanWrapperImpl nestedBw = getNestedBeanWrapperForPropertyPath(propertyName);
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		return getPropertyValue(tokens);
	}
	
	protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) {
		BeanWrapperImpl nestedBw = getNestedBeanWrapperForPropertyPath(propertyName);
		return nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
	}
	
	protected BeanWrapperImpl getNestedBeanWrapperForPropertyPath(String propertyPath) {
		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return nestedBw.getNestedBeanWrapperForPropertyPath(nestedPath);
		}
		return this;
	}

	public Object convertForProperty(Object value, String propertyName) {
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"未找到属性'" + propertyName + "'");
		}
		return this.typeConverterDelegate.convertIfNecessary(null, value, pd);
	}
	
	protected BeanWrapperImpl newNestedBeanWrapper(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}
	
	private void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		
		if (tokens.keys != null) {
			// 做一些事情
		}
		
		// 设置PropertyDescriptor缓存
		PropertyDescriptor pd = pv.resolvedDescriptor;
		if (pd == null || !pd.getWriteMethod().getDeclaringClass().isInstance(this.object)) {
			pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
			if (pd == null || pd.getWriteMethod() == null) {
				if (!pv.isOptional()) {
					throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName);
				}
			}
			pv.getOriginalPropertyValue().resolvedDescriptor = pd;
		}
		
		// 调用setter方法进行赋值
		Object oldValue = null;
		try {
			Object originalValue = pv.getValue();
			Object valueToApply = originalValue;
			
			if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
				if (pv.isConverted()) {
					valueToApply = pv.getConvertedValue();
				} else {
					if (isExtractOldValueForEditor() && pd.getReadMethod() != null) {
						final Method readMethod = pd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) && !readMethod.isAccessible()) {
							readMethod.setAccessible(true);
						}
						try {
							oldValue = readMethod.invoke(this.object);
						} catch (Exception e) {
							if (e instanceof PrivilegedActionException) {
								e = ((PrivilegedActionException) e).getException();
							}
							e.printStackTrace();
						}
					}
					valueToApply = this.typeConverterDelegate.convertIfNecessary(oldValue, originalValue, pd);
				}
				pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
			}
			
			final Method writeMethod = pd.getWriteMethod();
			if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()) && !writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			final Object value = valueToApply;
			writeMethod.invoke(this.object, value);
		} catch (InvocationTargetException ex) {
			PropertyChangeEvent propertyChangeEvent = 
				new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
			if (ex.getTargetException() instanceof ClassCastException) {
				throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
			}
			throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
		} catch (IllegalArgumentException e) {
			PropertyChangeEvent propertyChangeEvent = 
				new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
			throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), e);
		} catch (IllegalAccessException e) {
			PropertyChangeEvent propertyChangeEvent = 
				new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
			throw new MethodInvocationException(propertyChangeEvent, e);
		} catch (Exception e) {
			PropertyChangeEvent propertyChangeEvent = 
				new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
			throw new MethodInvocationException(propertyChangeEvent, e);
		}
	}
	
	private String getFinalPath(BeanWrapperImpl bw, String nestedPath) {
		if (bw == this) {
			return nestedPath;
		}
		return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
	}
	
	private BeanWrapperImpl getNestedBeanWrapper(String nestedPath) {
		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap<String, BeanWrapperImpl>();
		}
		
		// 处理nestedPath，去除[]
		PropertyTokenHolder tokens = getPropertyNameTokens(nestedPath);
		String canonicalName = tokens.canonicalName;
		
		// 调用getter方法获取属性值
		Object propertyValue = getPropertyValue(tokens);
		if (propertyValue == null) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
		}
		
		BeanWrapperImpl nestedBw = this.nestedBeanWrappers.get(canonicalName);
		if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
			nestedBw = newNestedBeanWrapper(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
			this.nestedBeanWrappers.put(canonicalName, nestedBw);
		}
		return nestedBw;
	}
	
	private Object getPropertyValue(PropertyTokenHolder tokens) {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
		if (pd == null || pd.getReadMethod() == null) {
			throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
		}
		final Method readMethod = pd.getReadMethod();
		try {
			if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) && !readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			Object value = readMethod.invoke(this.object, (Object[])null);
			
			if (tokens.keys != null) {
				if (value == null) {
					
				}
			}
			return value;
		} catch (InvocationTargetException e) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, 
					"调用getter方法获取属性'" + actualName + "'时发生异常", e);
		} catch (IllegalAccessException e) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, 
					"尝试访问属性'" + actualName + "'的getter方法时发生异常", e);
		} catch (Exception e) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, 
					"非法属性'" + actualName + "'", e);
		}
	}
	
	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		List<String> keys = new ArrayList<String>();
		
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
					if (key.startsWith("'") && key.endsWith("'") || key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = actualName != null ? actualName : propertyName;
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName += PROPERTY_KEY_PREFIX +
				StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_PREFIX + PROPERTY_KEY_SUFFIX) +
				PROPERTY_KEY_SUFFIX;
			tokens.keys = keys.toArray(new String[keys.size()]);
		}
		return tokens;
	}
	
	private class PropertyTokenHolder {
		
		public String canonicalName;
		
		public String actualName;
		
		public String[] keys;
	}

}
