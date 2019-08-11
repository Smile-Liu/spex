package org.spex.core.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * �����ļ����ع�����
 * @author hp
 *
 */
public class PropertiesLoaderUtil {

	
	public static Properties loadAllProperties(String location, ClassLoader classLoader) throws RuntimeException {
		if (location == null || "".equals(location)) {
			throw new IllegalArgumentException("�����ļ�δָ��");
		}
		
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		
		Properties props = new Properties();
		try {
			Enumeration<URL> urls = classLoader.getResources(location);
			
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				InputStream is = null;
				
				try {
					URLConnection conn = url.openConnection();
					conn.setUseCaches(false);
					is = conn.getInputStream();
					props.load(is);
				} finally {
					if (is != null) {
						is.close();
					}
				}
				
			}
			
			return props;
		} catch (IOException e) {
			throw new RuntimeException("��ȡ�����ļ�ʱIO�쳣", e);
		}
	}
}
