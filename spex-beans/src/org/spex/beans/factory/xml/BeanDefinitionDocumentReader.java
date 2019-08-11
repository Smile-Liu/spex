package org.spex.beans.factory.xml;

import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 处理Document，解析成BeanDefinition
 * @author hp
 * 目前仅支持beans嵌套bean标签
 * bean标签的属性和子标签：
 * 	属性：id、class、init-method、factory-bean
 * 	子标签：property（属性有name、value、ref；子标签有bean、list、map、set）
 */
public class BeanDefinitionDocumentReader {

	private XmlReaderContext readerContext;
	
	private static final String BEANS_ELEMENT = "beans";
	private static final String BEAN_ELEMENT = BeanDefinitionParseDelegate.BEAN_ELEMENT;
	
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		if (doc == null) {
			throw new IllegalArgumentException("Document is null");
		}
		
		this.readerContext = readerContext;
		Element root = doc.getDocumentElement();
		
		BeanDefinitionParseDelegate delegate = new BeanDefinitionParseDelegate(readerContext);
		parseBeanDefinitions(root, delegate);
	}
	
	protected void parseBeanDefinitions(Element root, BeanDefinitionParseDelegate delegate) {
		// 目前只支持标准的标签设置，不支持扩展
		if (delegate.isDefaultNamespace(delegate.getNamespaceUri(root))) {
			NodeList nodelist = root.getChildNodes();
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(delegate.getNamespaceUri(ele))) {
						parseDefaultElement(ele, delegate);
					}
				}
			}
		}
	}
	
	private void parseDefaultElement(Element ele, BeanDefinitionParseDelegate delegate) {
		if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		if (delegate.nodeNameEquals(ele, BEANS_ELEMENT)) {
			parseBeanDefinitions(ele, delegate);
		}
	}
	
	private void processBeanDefinition(Element ele, BeanDefinitionParseDelegate delegate) {
		BeanDefinitionHolder beanDefinitionHolder = delegate.parseBeanDefinitionElement(ele);
		if (beanDefinitionHolder != null) {
			// Spring源码中，首先会对非默认命名空间的属性和子标签进行装饰，这里就省略了
			
			// 注册到registry中
			String beanName = beanDefinitionHolder.getBeanName();
			BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
			this.readerContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
		}
	}
}
