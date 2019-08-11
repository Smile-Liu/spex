package org.spex.beans.factory.xml;

import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ����Document��������BeanDefinition
 * @author hp
 * Ŀǰ��֧��beansǶ��bean��ǩ
 * bean��ǩ�����Ժ��ӱ�ǩ��
 * 	���ԣ�id��class��init-method��factory-bean
 * 	�ӱ�ǩ��property��������name��value��ref���ӱ�ǩ��bean��list��map��set��
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
		// Ŀǰֻ֧�ֱ�׼�ı�ǩ���ã���֧����չ
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
			// SpringԴ���У����Ȼ�Է�Ĭ�������ռ�����Ժ��ӱ�ǩ����װ�Σ������ʡ����
			
			// ע�ᵽregistry��
			String beanName = beanDefinitionHolder.getBeanName();
			BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
			this.readerContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
		}
	}
}
