package org.spex.util;

import java.util.Comparator;

public interface PathMatcher {

	boolean match(String pattern, String path);
	
	Comparator<String> getPatternComparator(String path);
	
	String extractPathWithinPattern(String pattern, String path);
}
