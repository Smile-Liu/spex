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
 * ί�ɽ��д���
 *  xsd��schema��Ӧ·���Ĳ���
 * 
 * ע������xsd��publicIdΪnull��systemIdΪschemeLocationָ���ľ���xsd����
 * @author hp
 *
 */
public class DelegatingXsdEntityResolver implements EntityResolver {

	private final static String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spex-beans.schemas";
	
	private final String schemaMappingsLocation;
	
	private ClassLoader classLoader;
	
	// �洢schema�����Զ�Ӧ��schema-location��
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
				throw new FileNotFoundException(resourceLocation + "δ�ҵ�");
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
				// ���ж�һ���Ƿ�ֹ������ɺ����̸߳պ�ִ���긳ֵ�����µ���ִ��һ�ν���
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
