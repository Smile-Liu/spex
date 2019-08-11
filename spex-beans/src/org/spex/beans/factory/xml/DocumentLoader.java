package org.spex.beans.factory.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.spex.beans.factory.xml.constants.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

/**
 * 调用xml包，读取XML生成Document
 * @author hp
 */
public class DocumentLoader {

	/**
	 * JAXP attribute used to configure the schema language for validation.
	 */
	private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/**
	 * JAXP attribute value indicating the XSD schema language.
	 */
	private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

	
	public Document loadDocument(InputStream is, EntityResolver entityResolver, ErrorHandler errorHandler,
			int validationMode, boolean namespaceAware) throws Exception {
		
		DocumentBuilderFactory factory = createDocumentBuilderFactory(validationMode, namespaceAware);
		DocumentBuilder documentBuilder = createDocumentBuilder(factory, entityResolver, errorHandler);
		return documentBuilder.parse(is);
	}
	
	protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(namespaceAware);
		
		if (validationMode == XmlValidationModeDetector.VALIDATION_XSD) {
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
		}
		
		return factory;
	}
	
	protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory, EntityResolver entityResolver,
			ErrorHandler errorHandler) throws ParserConfigurationException {
		
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		
		if (entityResolver != null) {
			documentBuilder.setEntityResolver(entityResolver);
		}
		if (errorHandler != null) {
			documentBuilder.setErrorHandler(errorHandler);
		}
		return documentBuilder;
	}
}
