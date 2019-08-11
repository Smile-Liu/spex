package org.spex.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.spex.beans.factory.support.BeanDefinitionRegistry;
import org.spex.beans.factory.xml.constants.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * XML�����ļ���ȡ��
 * @author hp
 */
public class XmlBeanDefinitionReader {

	/**
	 * ָ��ΪXSD��֤ģʽ
	 */
	public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;
	
	private DocumentLoader documentLoader = new DocumentLoader();
	
	/**
	 * Resource Location
	 */
	private String path;
	private ClassLoader classLoader;
	
	private boolean namespaceAware;
	private EntityResolver entityResolver;
	private ErrorHandler errorHandler;
	
	/**
	 * BeanDefinitionע����
	 */
	private BeanDefinitionRegistry registry;
	
	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}
	
	public int loadBeanDefinitions(String resourceLocation) throws RuntimeException {
		if (resourceLocation == null || "".equals(resourceLocation)) {
			throw new IllegalArgumentException("�����ļ�����Ϊ��");
		}
		
		this.path = resourceLocation;
		this.classLoader = Thread.currentThread().getContextClassLoader();
		
		return this.doLoadBeanDefinitions(getInputStream());
	}
	
	public int doLoadBeanDefinitions(InputStream resource) throws RuntimeException {
		int validationMode = VALIDATION_XSD;
		try {
			Document document = this.documentLoader.loadDocument(resource, getEntityResolver(), getErrorHandler(), validationMode, false);
			return registerBeanDefinitions(document);
		} catch (ParserConfigurationException e) {
			
			throw new RuntimeException("��ȡ�����ļ�ʧ��", e);
		} catch (SAXException e) {

			throw new RuntimeException("�����ļ���ʽ����ȷ", e);
		} catch (IOException e) {

			throw new RuntimeException("�����ļ�IO�쳣", e);
		} catch (Exception e) {
			
			throw new RuntimeException("���������ļ�ʱ�����쳣", e);
		}
	}
	
	public int registerBeanDefinitions(Document doc) {
		BeanDefinitionDocumentReader documentReader = new BeanDefinitionDocumentReader();
		int countBefore = this.registry.getBeanDefinitionCount();
		documentReader.registerBeanDefinitions(doc, new XmlReaderContext(this));
		return this.registry.getBeanDefinitionCount() - countBefore;
	}
	
	public InputStream getInputStream() {
		return this.classLoader.getResourceAsStream(this.path);
	}

	public boolean isNamespaceAware() {
		return namespaceAware;
	}

	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	public EntityResolver getEntityResolver() {
		if (this.entityResolver == null) {
			this.entityResolver = new DelegatingXsdEntityResolver(this.classLoader);
		}
		return entityResolver;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public ErrorHandler getErrorHandler() {
		if (this.errorHandler == null) {
			this.errorHandler = new DefaultXsdErrorHandler();
		}
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public BeanDefinitionRegistry getRegistry() {
		return registry;
	}

	public String getResource() {
		return path;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
}
