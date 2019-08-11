package org.spex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {

	private static final char EXTENSION_SEPARATOR = '.';
	
	public static boolean hasText(String str) {
		return str != null && hasText((CharSequence) str);
	}
	
	private static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasLength(CharSequence str) {
		return str != null && str.length() > 0;
	}
	
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
		if (str == null) return null;
		
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		
		return tokens.toArray(new String[tokens.size()]);
	}
	
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}
	
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(str.substring(i, i + 1));
			}
		} else {
			int pos = 0;
			int delPos;
			
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(str.substring(pos, delPos));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos < str.length()) {
				result.add(str.substring(pos));
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}
	
	public static String arrayToDelimitedString(Object[] arr, String delimite) {
		if (arr == null) return "";
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delimite);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}
	
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		if (coll == null || coll.size() == 0) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		
		while(it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
	
	
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}
	
	public static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		
		StringBuilder sb = new StringBuilder(str.length());
		if (capitalize) {
			sb.append(Character.toUpperCase(str.charAt(0)));
		} else {
			sb.append(Character.toLowerCase(str.charAt(0)));
		}
		
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		
		return lcStr.equals(lcPrefix);
	}
	
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}
	
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		return sepIndex != -1 ? path.substring(0, sepIndex) : path;
	}
	
	public static String replace(String str, String oldPattern, String newPattern) {
		if (!hasLength(str) || !hasLength(oldPattern) || !hasLength(newPattern)) {
			return str;
		}
		
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		int idx = str.indexOf(oldPattern);
		int patLen = oldPattern.length();
		
		while (idx >= 0) {
			sb.append(str.substring(pos, idx));
			sb.append(newPattern);
			pos = idx + patLen;
			idx = str.indexOf(oldPattern, pos);
		}
		sb.append(str.substring(pos));
		
		return sb.toString();
	}

	public static String applyRelativePath(String uri, String path) {
		return null;
	}
}
