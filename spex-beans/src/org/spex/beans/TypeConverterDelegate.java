package org.spex.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.spex.beans.factory.support.PropertyTypeDescriptor;
import org.spex.core.CollectionFactory;
import org.spex.core.GenericCollectionTypeResolver;
import org.spex.core.MethodParameter;
import org.spex.core.convert.ConversionService;
import org.spex.core.convert.TypeDescriptor;
import org.spex.util.ClassUtils;
import org.spex.util.StringUtils;

/**
 * 类型转换工具类
 * @author hp
 *
 */
public class TypeConverterDelegate {

	private final PropertyEditorRegistrySupport propertyEditorRegistry;
	
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
		this.propertyEditorRegistry = propertyEditorRegistry;
	}
	
	public <T> T convertIfNecessary(Object newValue, Class<?> requiredType) {
		return convertIfNecessary(null, null, newValue, requiredType, TypeDescriptor.valueOf(requiredType));
	}

	public <T> T convertIfNecessary(Object newValue, Class<?> requiredType, MethodParameter methodParam) {
		return convertIfNecessary(null, null, newValue, requiredType, 
				methodParam != null ? new TypeDescriptor(methodParam) : TypeDescriptor.valueOf(requiredType));
	}
	
	public <T> T convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class<?> requiredType) {
		return convertIfNecessary(propertyName, oldValue, newValue, requiredType, TypeDescriptor.valueOf(requiredType));
	}
	
	public <T> T convertIfNecessary(Object oldValue, Object newValue, PropertyDescriptor pd) {
		return convertIfNecessary(pd.getName(), oldValue, newValue, pd.getPropertyType(), 
				new PropertyTypeDescriptor(pd, BeanUtils.getWriterMethodParameter(pd)));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T convertIfNecessary(String propertyName, Object oldValue, Object newValue,
			Class<?> requiredType, TypeDescriptor typeDescriptor) {
		
		Object convertedValue = newValue;
		
		PropertyEditor editor = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);
		
		// 没有找到属性编辑器（PropertyEditor），但指定了ConversionService
		ConversionService conversionService = this.propertyEditorRegistry.getConversionService();
		if (editor == null && conversionService != null && convertedValue != null) {
			TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(convertedValue);
			TypeDescriptor targetTypeDesc = typeDescriptor.forElementType(requiredType);
			
			if (conversionService.canConvert(sourceTypeDesc, targetTypeDesc)) {
				return (T) conversionService.convert(convertedValue, sourceTypeDesc, targetTypeDesc);
			}
		}
		
		// 值的类型不匹配需要的类型 newValue的类型不是requiredType
		if (editor != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
			// requiredType是集合，且newValue是String，且有参数
			if (requiredType != null && Collection.class.isAssignableFrom(requiredType) &&
					convertedValue instanceof String && typeDescriptor.getMethodParameter() != null) {
				
				Class<?> elemType = GenericCollectionTypeResolver.getCollectionParameterType(typeDescriptor.getMethodParameter());
				if (elemType != null && Enum.class.isAssignableFrom(elemType)) {
					convertedValue = StringUtils.commaDelimitedListToStringArray((String)convertedValue);
				}
			}
			
			if (editor == null) {
				editor = findDefaultEditor(requiredType, typeDescriptor);
			}
			convertedValue = doConvertValue(oldValue, newValue, requiredType, editor);
		}
		
		if (requiredType != null) {
			if (convertedValue != null) {
				
				// 要数组类型
				if (requiredType.isArray()) {
					if (convertedValue instanceof String && Enum.class.equals(requiredType.getComponentType())) {
						convertedValue = StringUtils.commaDelimitedListToStringArray((String) convertedValue);
					}
					return (T) convertToTypedArray(convertedValue, propertyName, requiredType.getComponentType());
				} else if (convertedValue instanceof Collection) {
					// 集合
					convertedValue = convertToTypedCollection(
							(Collection<?>) convertedValue, propertyName, requiredType, typeDescriptor);
				} else if (convertedValue instanceof Map) {
					// Map
					convertedValue = convertToTypedMap(
							(Map<?, ?>) convertedValue, propertyName, requiredType, typeDescriptor);
				}
				
				// 转换结果是数组，并且只要一个元素，则只要一个
				if (convertedValue.getClass().isArray() && Array.getLength(convertedValue) == 1) {
					convertedValue = Array.get(convertedValue, 0);
				}
				
				// 要求类型为String，并且转换值是简单类型或简单类型的包装类型，则直接toString
				if (String.class.equals(requiredType) && ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())) {
					return (T) convertedValue;
				} else if (convertedValue instanceof String && !requiredType.isInstance(convertedValue)){
					// 转换值是String，但要求类型不是String
					
					// 不是接口，且不是枚举
					if (!requiredType.isInterface() && !requiredType.isEnum()) {
						// 使用带String参数的构造器生成指定类型的对象
						try {
							Constructor<?> ctor = requiredType.getConstructor(String.class);
							return (T) BeanUtils.instantiateClass(ctor, convertedValue);
						} catch (NoSuchMethodException ex) {
							//
						} catch (Exception ex) {
							//
						}
					}
					
					// 要求类型为枚举
					if (requiredType.isEnum() && !StringUtils.hasText((String) convertedValue)) {
						return null;
					}
				}
			}
			
			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				throw new IllegalArgumentException("为属性'" + propertyName + "'把值转换为指定类型'" + requiredType.getName() + "'失败");
			}
		}
		
		return (T) convertedValue;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Collection<?> convertToTypedCollection(
			Collection<?> original, String propertyName, Class<?> requiredType, TypeDescriptor typeDescriptor) {
		
		boolean originalAllowed = requiredType.isInstance(original);
		if (!originalAllowed && !Collection.class.isAssignableFrom(requiredType)) {
			return original;
		}
		
		MethodParameter methodParam = typeDescriptor.getMethodParameter();
		Class<?> eleType = null;
		
		if (methodParam != null) {
			eleType = GenericCollectionTypeResolver.getCollectionParameterType(methodParam);
		}
		if (eleType == null && originalAllowed && 
				!this.propertyEditorRegistry.hasCustomEditorForElement(eleType, propertyName)) {
			return original;
		}
		
		Iterator<?> it;
		try {
			it = original.iterator();
			if (it == null) {
				return original;
			}
		} catch (Exception e) {
			return original;
		}
		
		Collection convertedCopy;
		try {
			if (CollectionFactory.isApproximableCollectionType(requiredType)) {
				convertedCopy = CollectionFactory.createApproximateCollection(original, original.size());
			} else {
				convertedCopy = (Collection<?>) requiredType.newInstance();
			}
		} catch (Throwable ex) {
			return original;
		}
		
		int i = 0;
		for (; it.hasNext(); i++) {
			Object element = it.next();
			String indexedPropertyName = buildIndexedPropertyName(propertyName, i);
			
			if (methodParam != null) {
				methodParam.increaseNestedLevel();
			}
			
			Object convertedElement = convertIfNecessary(indexedPropertyName, null, element, eleType, typeDescriptor);
			if (methodParam != null) {
				methodParam.decreaseNestedLevel();
			}
			
			try {
				convertedCopy.add(convertedElement);
			} catch (Throwable e) {
				return original;
			}
			
			originalAllowed = originalAllowed && (convertedElement == element);
		}
		
		return originalAllowed ? original : convertedCopy;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Map convertToTypedMap(
			Map original, String propertyName, Class<?> requiredType, TypeDescriptor typeDescriptor) {
		
		boolean originalAllowed = requiredType.isInstance(original);
		if (!originalAllowed && !Map.class.isAssignableFrom(requiredType)) {
			return original;
		}
		
		Class<?> keyType = null;
		Class<?> valueType = null;
		MethodParameter methodParam = typeDescriptor.getMethodParameter();
		
		if (methodParam != null) {
			keyType = GenericCollectionTypeResolver.getMapKeyParameterType(methodParam);
			valueType = GenericCollectionTypeResolver.getMapValueParameterType(methodParam);
		}
		if (keyType == null && valueType == null && originalAllowed &&
				!this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)) {
			return original;
		}
		
		Iterator it;
		try {
			it = original.entrySet().iterator();
			if (it == null) {
				return original;
			}
		} catch (Throwable e) {
			return original;
		}
		
		Map convertedCopy;
		try {
			if (CollectionFactory.isApproximableMapType(requiredType)) {
				convertedCopy = CollectionFactory.createApproximateMap(original, original.size());
			} else {
				convertedCopy = (Map) requiredType.newInstance();
			}
		} catch (Throwable e) {
			return original;
		}
		
		while (it.hasNext()) {
			Map.Entry entry = (Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			
			String keyedPropertyName = buildKeyedPropertyName(propertyName, key);
			
			if (methodParam != null) {
				methodParam.increaseNestedLevel();
				methodParam.setTypeIndexForCurrentLevel(0);
			}
			
			Object convertedKey = convertIfNecessary(keyedPropertyName, null, key, keyType, typeDescriptor);
			if (methodParam != null) {
				methodParam.setTypeIndexForCurrentLevel(1);
			}
			
			Object convertedValue = convertIfNecessary(keyedPropertyName, null, value, valueType, typeDescriptor);
			if (methodParam != null) {
				methodParam.decreaseNestedLevel();
			}
			
			try {
				convertedCopy.put(convertedKey, convertedValue);
			} catch (Throwable e) {
				return original;
			}
			
			originalAllowed = originalAllowed && (key == convertedKey) && (value == convertedValue);
		}
		return originalAllowed ? original : convertedCopy;
	}
	
	
	
	protected Object doConvertValue(Object oldValue, Object newValue, Class<?> requiredType, PropertyEditor editor) {
		Object convertedValue = newValue;
		
		if (editor != null && !(convertedValue instanceof String)) {
			Object newConvertedValue;
			
			editor.setValue(convertedValue);
			newConvertedValue = editor.getValue();
			
			if (convertedValue != newConvertedValue) {
				convertedValue = newConvertedValue;
				editor = null;
			}
		}
		
		Object returnValue = convertedValue;
		if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
			convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
		}
		
		if (convertedValue instanceof String) {
			if (editor != null) {
				String newTextValue = (String) convertedValue;
				return doConvertTextValue(oldValue, newTextValue, editor);
			} else if (String.class.equals(requiredType)) {
				returnValue = convertedValue;
			}
		}
		return returnValue;
	}
	
	protected Object doConvertTextValue(Object oldValue, String newTextValue, PropertyEditor editor) {
		editor.setValue(oldValue);
		editor.setAsText(newTextValue);
		return editor.getValue();
	}
	
	protected Object convertToTypedArray(Object input, String propertyName, Class<?> componentType) {
		if (input instanceof Collection) {
			Collection<?> coll = (Collection<?>)input;
			Object result = Array.newInstance(componentType, coll.size());
			int i = 0;
			for (Iterator<?> iter = coll.iterator(); iter.hasNext(); i++) {
				Object value = convertIfNecessary(buildIndexedPropertyName(propertyName, i), null, iter.next(), componentType);
				Array.set(result, i, value);
			}
			return result;
		} else if (input.getClass().isArray()) {
			// 本身就是数组
			// 如果类型相同，且没有指定PropertyEditor，则表示匹配无误
			if (componentType.equals(input.getClass().getComponentType()) &&
					!this.propertyEditorRegistry.hasCustomEditorForElement(componentType, propertyName)) {
				return input;
			}
			
			int arrLength = Array.getLength(input);
			Object result = Array.newInstance(componentType, arrLength);
			for (int i = 0; i < arrLength; i++) {
				Object value = convertIfNecessary(
						buildIndexedPropertyName(propertyName, i), null, Array.get(input, i), componentType);
				Array.set(result, i, value);
			}
			return result;
		} else {
			Object result = Array.newInstance(componentType, 1);
			Object value = convertIfNecessary(
					buildIndexedPropertyName(propertyName, 0), null, input, componentType);
			Array.set(result, 0, value);
			return result;
		}
	}
	
	protected PropertyEditor findDefaultEditor(Class<?> requiredType, TypeDescriptor typeDescriptor) {
		if (requiredType != null) {
			PropertyEditor editor = this.propertyEditorRegistry.getDefaultEditor(requiredType);
			
			if (editor == null && !String.class.equals(requiredType)) {
				editor = BeanUtils.findEditorByConvention(requiredType);
			}
			return editor;
		}
		return null;
	}
	
	private String buildIndexedPropertyName(String propertyName, int index) {
		return propertyName != null ? propertyName + "[" + index + "]" : null;
	}
	
	private String buildKeyedPropertyName(String propertyName, Object key) {
		return propertyName != null ? propertyName + "[" + key + "]" : null;
	}
}
