package org.spex.beans.factory;

import java.util.ArrayList;
import java.util.List;

import org.spex.beans.PropertyValue;
import org.spex.beans.PropertyValues;

public class MutablePropertyValues implements PropertyValues {
	
	private final List<PropertyValue> propertyValueList;
	
	private volatile boolean converted = false;
	
	public MutablePropertyValues() {
		propertyValueList = new ArrayList<PropertyValue>();
	}
	
	public MutablePropertyValues(PropertyValues original) {
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<PropertyValue>();
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(pv);
			}
		} else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}
	
	public MutablePropertyValues(List<PropertyValue> propertyValueList) {
		this.propertyValueList = 
			(propertyValueList != null ? propertyValueList : new ArrayList<PropertyValue>());
	}
	
	public void add(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}
	
	public void addPropertyValue(PropertyValue propertyValue) {
		this.propertyValueList.add(propertyValue);
	}
	
	@Override
	public boolean contains(String propertyName) {
		return getPropertyValue(propertyName) != null;
	}
	
	public List<PropertyValue> getPropertyValueList() {
		return propertyValueList;
	}

	@Override
	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}

	@Override
	public PropertyValue getPropertyValue(String propertyName) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue pv = this.propertyValueList.get(i);
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}
	
	public int size() {
		return this.propertyValueList.size();
	}

	public boolean isConverted() {
		return converted;
	}

	public void setConverted() {
		this.converted = true;
	}
	
	
}
