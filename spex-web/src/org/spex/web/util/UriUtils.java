package org.spex.web.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.spex.util.StringUtils;

public class UriUtils {

	public static String decode(String source, String encoding) throws UnsupportedEncodingException {
		if (!StringUtils.hasText(source) || !StringUtils.hasText(encoding)) {
			return null;
		}
		
		int length = source.length();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
		
		for (int i = 0; i < length; i++) {
			char ch = source.charAt(i);
			if (ch == '%') {
				// Get请求的参数传递时，会将中文转换为%开头并且其后跟至少两位字符的字符串
				if ((i + 2) < length) {
					char hex1 = source.charAt(i + 1);
					char hex2 = source.charAt(i + 2);
					
					int d1 = Character.digit(hex1, 16);
					int d2 = Character.digit(hex2, 16);
					
					bos.write((char)(d1 << 4 + d2));
					i += 2;
				} else {
					throw new IllegalArgumentException(source + "编码不正确");
				}
			} else {
				bos.write(ch);
			}
		}
		return new String(bos.toByteArray(), encoding);
	}
}
