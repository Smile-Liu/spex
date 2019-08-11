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
		message.append("配置出错：");
		message.append(msg);
		message.append("，相关文件：");
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
