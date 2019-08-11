package org.spex.beans.factory.xml;

import org.spex.beans.factory.parsing.BeanDefinitionParsingException;
import org.spex.beans.factory.parsing.ParseState;
import org.spex.beans.factory.support.BeanDefinitionRegistry;

public class XmlReaderContext {

	private XmlBeanDefinitionReader reader;
	
	public XmlReaderContext(XmlBeanDefinitionReader reader) {
		this.reader = reader;
	}
	
	public void error(String msg, Object source) {
		error(msg, source, null, null);
	}
	
	public void error(String msg, Object source, Throwable ex) {
		error(msg, source, null, ex);
	}
	
	public void error(String msg, Object source, ParseState parseState, Throwable ex) {
		StringBuilder message = new StringBuilder();
		message.append("���ó���");
		message.append(msg);
		message.append("������ļ���");
		message.append(this.reader.getResource());
		if (parseState != null) {
			message.append(parseState.toString());
		}
		throw new BeanDefinitionParsingException(message.toString(), ex);
	}
	
	public BeanDefinitionRegistry getRegistry() {
		return this.reader.getRegistry();
	}

	public XmlBeanDefinitionReader getReader() {
		return reader;
	}

}
