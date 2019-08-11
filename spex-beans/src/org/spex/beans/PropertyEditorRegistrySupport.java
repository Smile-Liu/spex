package org.spex.beans;

import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.spex.beans.propertyeditors.CustomCollectionEditor;
import org.spex.core.convert.ConversionService;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

	private ConversionService conversionService;
	
	private Map<Class<?>, PropertyEditor> defaultEditors;
	
	private Map<Class<?>, PropertyEditor> customEditors;
	
	@Override
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		
	}

	@Override
	public void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor) {
		
	}

	@Override
	public PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath) {
		Class<?> requiredTypeToUse = requiredType;
		if (propertyPath != null) {
			return null;
		}
		return getCustomEditor(requiredTypeToUse);
	}

	public boolean hasCustomEditorForElement(Class<?> elemType, String propertyPath) {
		
		return elemType != null && this.customEditors != null && this.customEditors.containsKey(elemType);
	}
	
	public PropertyEditor getDefaultEditor(Class<?> requiredType) {
		if (requiredType == null) {
			return null;
		}
		
		if (this.defaultEditors == null) {
			doRegisterDefaultEditors();
		}
		return this.defaultEditors.get(requiredType);
	}
	
	private void doRegisterDefaultEditors() {
		this.defaultEditors = new HashMap<Class<?>, PropertyEditor>();
		
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
	}
	
	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	private PropertyEditor getCustomEditor(Class<?> requiredType) {
		if (requiredType == null || this.customEditors == null) {
			return null;
		}
		
		return null;
	}
}
