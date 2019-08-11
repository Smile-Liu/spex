package org.spex.util;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntPathMatcher implements PathMatcher {

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");
	
	private static final String DEFAULT_PATH_SEPARATOR = "/";
	
	private String pathSeparator = DEFAULT_PATH_SEPARATOR;
	
	
	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR;
	}

	@Override
	public boolean match(String pattern, String path) {
		return false;
	}

	protected boolean doMatch(String pattern, String path, boolean fullMatch) {
		if (path.startsWith(pathSeparator) != pattern.startsWith(pathSeparator)) {
			return false;
		}
		
		String[] pattDirs = StringUtils.tokenizeToStringArray(pattern, pathSeparator);
		String[] pathDirs = StringUtils.tokenizeToStringArray(path, pathSeparator);
		
		int pattIdxStart = 0;
		int pattIdxEnd = pattDirs.length - 1;
		int pathIdxStart = 0;
		int pathIdxEnd = pathDirs.length - 1;
		
		while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			String patDir = pattDirs[pattIdxStart];
			if ("**".equals(patDir)) {
				break;
			}
			if (!matchStrings(patDir, pathDirs[pathIdxStart])) {
				return false;
			}
			pattIdxStart++;
			pathIdxStart++;
		}
		
		// 请求路径先匹配完成
		if (pathIdxStart > pathIdxEnd) {
			// 如果模式字符串也匹配完成
			if (pattIdxStart > pattIdxEnd) {
				return pattern.endsWith(this.pathSeparator) ? path.endsWith(this.pathSeparator) : 
					!path.endsWith(this.pathSeparator);
			}
			
			// 不需要全匹配
			if (!fullMatch) {
				return true;
			}
			
			// 正好匹配到模式的结尾，如果结尾是*，并且路径结尾是/，则通过
			if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(this.pathSeparator)) {
				return true;
			}
			
			// 路径匹配完成但模式没有匹配完，则剩下的模式字符只能是**/才能通过
			for (int i = pattIdxStart; i < pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			
			return true;
		} 
		else if (pattIdxStart > pattIdxEnd) {
			// 如果只有模式字符匹配完成，则不通过
			return false;
		}
		else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
			// 都没匹配完成，但不要求完全匹配且是以“**”退出的，则通过
			return true;
		}
		
		// 模式字符从头匹配到“**”，现在从尾匹配
		while (pathIdxStart <= pathIdxEnd && pattIdxStart <= pattIdxEnd) {
			String patDir = pattDirs[pattIdxEnd];
			if (patDir.equals("**")) {
				break;
			}
			if (!matchStrings(patDir, pathDirs[pathIdxEnd])) {
				return false;
			}
			pathIdxEnd--;
			pattIdxEnd--;
		}
		
		// 如果倒序情况下，路径匹配完成
		if (pathIdxStart > pathIdxEnd) {
			for (int i = pattIdxStart; i < pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		}
		
		// 路径没有匹配完成，是以“**”结束的或者是模式匹配完了
		// 只有前后都有 ** 才会到达这里
		while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			int patIdxTmp = -1;
			for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
				if (pattDirs[i].equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			
			// 是break出来的，所以是 **/** ，继续查看后续的模式
			if (patIdxTmp == pattIdxStart + 1) {
				pattIdxStart++;
				continue;
			}
			
			// 当前要匹配的模式字符串还有几个
			int patLength = (patIdxTmp - pattIdxStart - 1);
			// 当前要匹配的路径字符串还有几个
			int strLength = pathIdxEnd - pathIdxStart + 1;
			int foundIdx = 1;
			
			strLoop:
				for (int i = 0; i < strLength - patLength; i++) {
					for (int j = 0; j < patLength; j++) {
						String subPat = pattDirs[pattIdxStart + j + 1];
						String subStr = pathDirs[pathIdxStart + i + j];
						if (!matchStrings(subPat, subStr)) {
							continue strLoop;
						}
					}
					foundIdx = pathIdxStart + 1;
					break;
				}
			
			if (foundIdx == -1) {
				return false;
			}
			
			pattIdxStart = patIdxTmp;
			pathIdxStart = foundIdx + patLength;
		}
		
		for (int i = pattIdxStart; i < pattIdxEnd; i++) {
			if (!pattDirs[i].equals("**")) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean matchStrings(String pattern, String path) {
		AntPathStringMatcher matcher = new AntPathStringMatcher(pattern, path);
		return matcher.matchStrings();
	}
	
	@Override
	public String extractPathWithinPattern(String pattern, String path) {
		String[] patternParts = StringUtils.delimitedListToStringArray(pattern, pathSeparator);
		String[] pathParts = StringUtils.delimitedListToStringArray(path, pathSeparator);
		
		StringBuilder builder = new StringBuilder();
		
		int puts = 0;
		for (int i = 0; i < patternParts.length; i++) {
			String patternPart = patternParts[i];
			if ((patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) && pathParts.length >= i + 1) {
				if (puts > 0 || (i == 0 && !pattern.startsWith(pathSeparator))) {
					builder.append(this.pathSeparator);
				}
				builder.append(pathParts[i]);
				puts++;
			}
		}
		
		for (int i = patternParts.length; i < pathParts.length; i++) {
			if (puts > 0 || i > 0) {
				builder.append(this.pathSeparator);
			}
			builder.append(pathParts[i]);
		}
		return builder.toString();
	}
	
	@Override
	public Comparator<String> getPatternComparator(String path) {
		return new AntPatternComparator(path);
	}
	
	private static class AntPatternComparator implements Comparator<String> {

		private final String path;
		
		public AntPatternComparator(String path) {
			this.path = path;
		}
		
		@Override
		public int compare(String o1, String o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return 1;
			} else if (o2 == null) {
				return -1;
			}
			
			boolean o1EqualsPath = o1.equals(path);
			boolean o2EqualsPath = o2.equals(path);
			
			if (o1EqualsPath && o2EqualsPath) {
				return 0;
			} else if (o1EqualsPath) {
				return -1;
			} else if (o2EqualsPath) {
				return 1;
			}
			
			// 通配符的数量
			int o1WildCardCount = getWildCardCount(o1);
			int o2WildCardCount = getWildCardCount(o2);
			
			// 大括号的数量
			int o1BracketCount = StringUtils.countOccurrencesOf(o1, "{");
			int o2BracketCount = StringUtils.countOccurrencesOf(o2, "{");
			
			int o1TotalCount = o1WildCardCount + o1BracketCount;
			int o2TotalCount = o2WildCardCount + o2BracketCount;
			
			if (o1TotalCount != o2TotalCount) {
				return o1TotalCount - o2TotalCount;
			}
			
			int o1Length = getPatternLength(o1);
			int o2Length = getPatternLength(o2);
			
			if (o1Length != o2Length) {
				return o2Length - o1Length;
			}
			
			if (o1WildCardCount < o2WildCardCount) {
				return -1;
			} else if (o1WildCardCount > o2WildCardCount) {
				return 1;
			}
			
			if (o1BracketCount < o2BracketCount) {
				return -1;
			} else if (o1BracketCount > o2BracketCount) {
				return 1;
			}
			
			return 0;
		}
		
		private int getWildCardCount(String pattern) {
			if (pattern.endsWith(".*")) {
				pattern = pattern.substring(0, pattern.length() - 2);
			}
			return StringUtils.countOccurrencesOf(pattern, "*");
		}
		
		private int getPatternLength(String pattern) {
			Matcher m = VARIABLE_PATTERN.matcher(pattern);
			return m.replaceAll("#").length();
		}
	}
}
