package org.spex.beans.factory.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.spex.core.io.support.CollectionUtils;
import org.spex.core.io.support.PropertiesLoaderUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 委派进行处理：
 *  xsd的schema对应路径的查找
 * 
 * 注：对于xsd，publicId为null，systemId为schemeLocation指定的具体xsd名称
 * @author hp
 *
 */
public class DelegatingXsdEntityResolver implements EntityResolver {

	private final static String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spex-beans.schemas";
	
	private final String schemaMappingsLocation;
	
	private ClassLoader classLoader;
	
	// 存储schema的属性对应（schema-location）
	private volatile Map<String, String> schemaMappings;
	
	public DelegatingXsdEntityResolver(ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.schemaMappingsLocation = DEFAULT_SCHEMA_MAPPINGS_LOCATION;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		
		String resourceLocation = getSchemaMappings().get(systemId);
		if (resourceLocation != null) {
			InputStream is = this.classLoader.getResourceAsStream(resourceLocation);
			if (is == null) {
				throw new FileNotFoundException(resourceLocation + "未找到");
			}
				
			InputSource inputSource = new InputSource(is);
			inputSource.setPublicId(publicId);
			inputSource.setSystemId(systemId);
			
			return inputSource;
		}
		return null;
	}

	
	private Map<String, String> getSchemaMappings() {
		if (this.schemaMappings == null) {
			synchronized (this) {
				// 再判断一次是防止加锁完成后别的线程刚好执行完赋值而导致的再执行一次解析
				if (this.schemaMappings == null) {
					
					Properties mappings = 
						PropertiesLoaderUtil.loadAllProperties(this.schemaMappingsLocation, this.classLoader);
					
					Map<String, String> schemaMapping = new ConcurrentHashMap<String, String>();
					CollectionUtils.mergeProperties2Map(mappings, schemaMapping);
					this.schemaMappings = schemaMapping;
				}
			}
		}
		return this.schemaMappings;
	}
	
}
