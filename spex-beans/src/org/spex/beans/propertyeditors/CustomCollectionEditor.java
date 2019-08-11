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
			throw new IllegalArgumentException("�������Ͳ���Ϊ��");
		}
		if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException("ָ������[" + collectionType.getName() + "]���Ǽ���");
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
			// ת������Ԫ��
			Collection<?> source = (Collection<?>) value;
			Collection target = createCollection(this.collectionType, source.size());
			
			for (Object elem : source) {
				target.add(convertElement(elem));
			}
			super.setValue(target);
		} else if (value.getClass().isArray()) {
			// ������Ԫ��ת��Ϊ����Ԫ��
			int length = Array.getLength(value);
			Collection target = createCollection(this.collectionType, length);
			
			for (int i = 0; i < length; i++) {
				target.add(convertElement(Array.get(value, i)));
			}
			super.setValue(target);
		} else {
			// ��������������ת��Ϊ��һԪ�صļ���
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
				throw new IllegalArgumentException("����ʵ��������[" + collectionType + "]:" + e.getMessage());
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
