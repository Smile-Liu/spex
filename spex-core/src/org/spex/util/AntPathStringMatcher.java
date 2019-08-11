package org.spex.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntPathStringMatcher {

	private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{([^/]+?)\\}");
	
	private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";
	
	private final Pattern pattern;
	
	private String str;
	
	public AntPathStringMatcher(String pattern, String str) {
		this.str = str;
		this.pattern = createPattern(pattern);
	}
	
	public boolean matchStrings() {
		Matcher m = pattern.matcher(str);
		if (m.matches()) {
			return true;
		}
		return false;
	}
	
	private Pattern createPattern(String pattern) {
		StringBuilder patternBuilder = new StringBuilder();
		
		Matcher m = GLOB_PATTERN.matcher(pattern);
		int end = 0;
		while (m.find()) {
			patternBuilder.append(quote(pattern, end, m.start()));
			
			String match = m.group();
			if ("?".equals(match)) {
				patternBuilder.append('.');
			}
			else if ("*".equals(match)) {
				patternBuilder.append(".*");
			}
			else if (match.startsWith("{") && match.endsWith("}")) {
				int colonIdx = match.indexOf(':');
				if (colonIdx == -1) {
					patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
				}
				else {
					String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
					patternBuilder.append('(');
					patternBuilder.append(variablePattern);
					patternBuilder.append(')');
				}
			}
			end = m.end();
		}
		patternBuilder.append(quote(pattern, end, pattern.length()));
		return Pattern.compile(patternBuilder.toString());
	}
	
	private String quote(String s, int start, int end) {
		if (start == end) {
			return "";
		}
		return Pattern.quote(s.substring(start, end));
	}
}
