package org.spex.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spex.util.ClassUtils;

public class ConstructorArgumentValues {

	private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<Integer, ValueHolder>();
	private final List<ValueHolder> genericArgumentValues = new LinkedList<ValueHolder>();
	
	public ConstructorArgumentValues() {}
	
	public boolean hasIndexedArgumentValue(int index) {
		return this.indexedArgumentValues.containsKey(index);
	}
	
	public void addIndexedArgumentValue(int index, ValueHolder valueHolder) {
		this.indexedArgumentValues.put(index, valueHolder);
	}
	
	public void addGenericArgumentValue(ValueHolder valueHolder) {
		this.genericArgumentValues.add(valueHolder);
	}
	
	public int getArgumentCount() {
		return indexedArgumentValues.size() + genericArgumentValues.size();
	}
	
	public boolean isEmpty() {
		return this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty();
	}
	
	public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
		ValueHolder valueHolder = this.getIndexedArgumentValue(index, requiredType, requiredName);
		if (valueHolder == null) {
			valueHolder = this.getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
		}
		return valueHolder;
	}
	
	public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType, String requiredName) {
		ValueHolder valueHolder = this.getIndexedArgumentValues().get(index);
		if (valueHolder != null &&
				(valueHolder.getType() == null || 
						requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType())) &&
				(valueHolder.getName() == null ||
						requiredName != null && requiredName.equals(valueHolder.getName()))) {
			return valueHolder;
		}
		return null;
	}
	
	public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
		// 遍历一般参数
		for (ValueHolder valueHolder : this.genericArgumentValues) {
			// 包含在已使用的集合中
			if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
				continue;
			}
			
			// 名称不匹配
			if (valueHolder.getName() != null && (requiredName == null || !requiredName.equals(valueHolder.getName()))) {
				continue;
			}
			
			// 类型不匹配
			if (valueHolder.getType() != null && (requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
				continue;
			}
			
			// holder的value值的类型和requiredType不匹配
			if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
					!ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
				continue;
			}
			
			return valueHolder;
		}
		return null;
	}
	
	
	public Map<Integer, ValueHolder> getIndexedArgumentValues() {
		return indexedArgumentValues;
	}

	public List<ValueHolder> getGenericArgumentValues() {
		return genericArgumentValues;
	}

	public static class ValueHolder {
		private String name;
		private Object value;
		private String type;
		private Object source;
		
		private boolean converted = false;
		
		private Object convertedValue;
		
		public ValueHolder(Object value) {
			this.value = value;
		}
		public ValueHolder(Object value, String type) {
			this.value = value;
			this.type = type;
		}
		public ValueHolder(String name, Object value, String type) {
			this.name = name;
			this.value = value;
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		public Object getSource() {
			return source;
		}
		public void setSource(Object source) {
			this.source = source;
		}
		public synchronized boolean isConverted() {
			return converted;
		}
		public synchronized void setConvertedValue(Object value) {
			this.convertedValue = value;
			this.converted = true;
		}
		public synchronized Object getConvertedValue() {
			return convertedValue;
		}
		
	}
}
