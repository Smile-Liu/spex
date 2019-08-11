package org.spex.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CustomCollectionEditor extends PropertyEditorSupport {
	
	private final Class<?> collectionType;
	
	private final boolean nullAsEmptyCollection;
	
	
	public CustomCollectionEditor(Class<?> collectionType) {
		this(collectionType, false);
	}

	public CustomCollectionEditor(Class<?> collectionType, boolean nullAsEmptyCollection) {
		if (collectionType == null) {
			throw new IllegalArgumentException("集合类型不能为空");
		}
		if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException("指定类型[" + collectionType.getName() + "]不是集合");
		}
		this.collectionType = collectionType;
		this.nullAsEmptyCollection = nullAsEmptyCollection;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValue(Object value) {
		if (value == null && this.nullAsEmptyCollection) {
			super.setValue(createCollection(this.collectionType, 0));
		} else if (value == null || (this.collectionType.isInstance(value) && !alwaysCreateNewCollection())) {
			super.setValue(value);
		} else if (value instanceof Collection) {
			// 转化集合元素
			Collection<?> source = (Collection<?>) value;
			Collection target = createCollection(this.collectionType, source.size());
			
			for (Object elem : source) {
				target.add(convertElement(elem));
			}
			super.setValue(target);
		} else if (value.getClass().isArray()) {
			// 把数组元素转化为集合元素
			int length = Array.getLength(value);
			Collection target = createCollection(this.collectionType, length);
			
			for (int i = 0; i < length; i++) {
				target.add(convertElement(Array.get(value, i)));
			}
			super.setValue(target);
		} else {
			// 其他情况，则把它转化为单一元素的集合
			Collection target = createCollection(this.collectionType, 1);
			target.add(convertElement(value));
			super.setValue(target);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected Collection<?> createCollection(Class<?> collectionType, int initialCapacity) {
		if (!collectionType.isInterface()) {
			try {
				return (Collection<?>) collectionType.newInstance();
			} catch (Exception e) {
				throw new IllegalArgumentException("不能实例化类型[" + collectionType + "]:" + e.getMessage());
			}
		}
		
		if (List.class.equals(collectionType)) {
			return new ArrayList(initialCapacity);
		}
		
		if (SortedSet.class.equals(collectionType)) {
			return new TreeSet();
		}
		
		return new LinkedHashSet(initialCapacity);
	}
	
	protected Object convertElement(Object element) {
		return element;
	}
	
	protected boolean alwaysCreateNewCollection() {
		return false;
	}
}
