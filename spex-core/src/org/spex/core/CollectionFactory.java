package org.spex.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionFactory {

	private static final Set<Class<?>> approximableCollectionTypes = new HashSet<Class<?>>(10);
	
	private static final Set<Class<?>> approximableMapTypes = new HashSet<Class<?>>(10);
	
	static {
		approximableCollectionTypes.add(Collection.class);
		approximableCollectionTypes.add(List.class);
		approximableCollectionTypes.add(Set.class);
		approximableCollectionTypes.add(SortedSet.class);
		approximableCollectionTypes.add(ArrayList.class);
		approximableCollectionTypes.add(LinkedList.class);
		approximableCollectionTypes.add(HashSet.class);
		approximableCollectionTypes.add(LinkedHashSet.class);
		approximableCollectionTypes.add(TreeSet.class);
		
		approximableMapTypes.add(Map.class);
		approximableMapTypes.add(HashMap.class);
		approximableMapTypes.add(SortedMap.class);
		approximableMapTypes.add(LinkedHashMap.class);
		approximableMapTypes.add(TreeMap.class);
	}
	
	public static boolean isApproximableCollectionType(Class<?> collectionType) {
		return collectionType != null && approximableCollectionTypes.contains(collectionType);
	}

	@SuppressWarnings("unchecked")
	public static Collection<?> createApproximateCollection(Collection<?> collection, int initialCapacity) {
		if (collection instanceof LinkedList) {
			return new LinkedList<Object>();
		}
		if (collection instanceof List) {
			return new ArrayList<Object>(initialCapacity);
		}
		if (collection instanceof SortedSet) {
			return new TreeSet<Object>(((SortedSet<Object>) collection).comparator());
		}
		return new LinkedHashSet<Object>(initialCapacity);
	}

	public static boolean isApproximableMapType(Class<?> mapType) {
		return mapType != null && approximableMapTypes.contains(mapType);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map createApproximateMap(Map<?, ?> map, int initialCapacity) {
		if (map instanceof SortedMap) {
			return new TreeMap(((SortedMap) map).comparator());
		}
		return new LinkedHashMap(initialCapacity);
	}
}
