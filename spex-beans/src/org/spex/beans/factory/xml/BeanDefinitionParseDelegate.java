package org.spex.beans.factory.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spex.beans.factory.config.AbstractBeanDefinition;
import org.spex.beans.factory.config.BeanDefinition;
import org.spex.beans.factory.config.BeanDefinitionHolder;
import org.spex.beans.factory.config.ConstructorArgumentValues;
import org.spex.beans.factory.config.RuntimeBeanReference;
import org.spex.beans.factory.config.TypedStringValue;
import org.spex.beans.factory.support.GenericBeanDefinition;
import org.spex.util.ClassUtils;
import org.spex.util.StringUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 解析BeanDefinition的代理
 * @author hp
 *
 */
public class BeanDefinitionParseDelegate {

	public static final String BEAN_ELEMENT = "bean";
	
	private static final String DEFAULT_NAMESPACE_URI = "http://www.spex.liu/schema/beans";
	
	private static final String BEAN_NAME_DELIMITERS = ",;";
	
	private static final String ID_ATTRIBUTE = "id";
	
	private static final String CLASS_ATTRIBUTE = "class";
	
	private static final String INIT_METHOD_ATTRIBUTE = "init-method";

	private static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";
	
	private static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";
	
	private static final String SCOPE_SINGLETON = "singleton";
	
	private static final String AUTOWIRE_ATTRIBUTE = "autowire";
	private static final String AUTOWIRE_BYNAME_VALUE = "byName";
	private static final String AUTOWIRE_BYTYPE_VALUE = "byType";
	
	private static final String PRIMARY_ATTRIBUTE = "primary";
	private static final String DEPENDSON_ATTRIBUTE = "depends-on";

	private static final String TRUE_VALUE = "true";
	
	private static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	private static final String INDEX_ATTRIBUTE = "index";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String NAME_ATTRIBUTE = "name";
	
	private static final String REF_ATTRIBUTE = "ref";
	private static final String VALUE_ATTRIBUTE = "value";
	
	private static final String LIST_ELEMENT = "list";
	private static final String SET_ELEMENT = "set";
	private static final String MAP_ELEMENT = "map";
	private static final String VALUE_ELEMENT = "value";
	private static final String ENTRY_ELEMENT = "entry";
	private static final String KEY_ATTRIBUTE = "key";
	private static final String KEY_TYPE_ATTRIBUTE = "key-type";
	private static final String VALUE_TYPE_ATTRIBUTE = "value-type";
	
	private static final String PROPERTY_ELEMENT = "property";
	private static final String GENERATED_BEAN_NAME_SEPARATOR = "#";
	
	/**
	 * 存储使用过的BeanName，方便校验BeanName的唯一性
	 */
	private final Set<String> usedNames = new HashSet<String>();
	
	private XmlReaderContext readerContext;
	
	public BeanDefinitionParseDelegate(XmlReaderContext readerContext) {
		this.readerContext = readerContext;
	}
	
	/**
	 * 判断是否是默认指定的命名空间
	 * @param namespace 指定节点的命名空间
	 * @return 是否
	 */
	public boolean isDefaultNamespace(String namespace) {
		return StringUtils.hasText(namespace) && DEFAULT_NAMESPACE_URI.equals(namespace);
	}
	
	/**
	 * 获得标签的命名空间：xmlns属性的值
	 * @param node 节点（一般是指根节点）
	 * @return 命名空间
	 */
	public String getNamespaceUri(Node node) {
		return node.getNamespaceURI();
	}
	
	/**
	 * 判断标签名是否是指定的标签名
	 *  LocalName是前缀+冒号+标签名里面的标签名，例如：context:a的标签名是a
	 * @param node 节点
	 * @param desiredName 指定的标签名
	 * @return 是否
	 */
	public boolean nodeNameEquals(Node node, String desiredName) {
		return desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName());
	}
	
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
		return parseBeanDefinitionElement(ele, null);
	}
	
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
		String beanName = ele.getAttribute(ID_ATTRIBUTE);
		
		if (containingBean == null) {
			checkBeanNameUniqueness(beanName, ele);
		}
		
		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, containingBean, beanName);
		if (beanDefinition != null) {
			
			// 查看是否有name，没有的话生成一个
			if (!StringUtils.hasText(beanName)) {
				beanName = generateBeanName(beanDefinition, containingBean != null);
			}
			
			return new BeanDefinitionHolder(beanDefinition, beanName);
		}
		
		return null;
	}
	
	public AbstractBeanDefinition parseBeanDefinitionElement(Element ele, BeanDefinition containingBean, String beanName) {
		
		String className = ele.getAttribute(CLASS_ATTRIBUTE);
		if (StringUtils.hasText(className)) {
			className = className.trim();
		}
		
		try {
			AbstractBeanDefinition beanDefinition = createBeanDefinition(className);
			// 属性
			parseBeanDefinitionAttributes(ele, beanDefinition);
			// 子标签-构造器标签construct-arg
			parseConstructorArgElements(ele, beanDefinition);
			// 子标签-property
			parsePropertyElements(ele, beanDefinition);
			
			return beanDefinition;
		} catch (ClassNotFoundException e) {
			this.readerContext.error("未找到类 [" + className + "]", ele, e);
		} catch (Exception e) {
			this.readerContext.error("在解析bean的过程中发生未知异常", ele, e);
		}
		
		return null;
	}
	
	public AbstractBeanDefinition createBeanDefinition(String className) throws ClassNotFoundException {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		
		ClassLoader classLoader = this.readerContext.getReader().getClassLoader();
		if (StringUtils.hasText(className)) {
			if (classLoader != null) {
				try {
					beanDefinition.setBeanClass(classLoader.loadClass(className));
				} catch (ClassNotFoundException e) {
					throw e;
				}
			}
			beanDefinition.setBeanClassName(className);
		}
		return beanDefinition;
	}
	
	public void parseBeanDefinitionAttributes(Element ele, AbstractBeanDefinition bd) {
		// 当前仅支持单例模式
		bd.setScope(SCOPE_SINGLETON);
		
		String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
		bd.setAutowireMode(getAutowireMode(autowire));
		
		if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
			bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
		}
		
		if (ele.hasAttribute(DEPENDSON_ATTRIBUTE)) {
			String dependsOn = ele.getAttribute(DEPENDSON_ATTRIBUTE);
			bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, BEAN_NAME_DELIMITERS));
		}
		
		if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
			String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
			if (StringUtils.hasText(initMethodName)) {
				bd.setInitMethodName(initMethodName);
			}
		}
		
		if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
			String factoryBeanName = ele.getAttribute(FACTORY_BEAN_ATTRIBUTE);
			if (StringUtils.hasText(factoryBeanName)) {
				bd.setFactoryBeanName(factoryBeanName);
			}
		}
		
		if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
			String factoryMethodName = ele.getAttribute(FACTORY_METHOD_ATTRIBUTE);
			if (StringUtils.hasText(factoryMethodName)) {
				bd.setFactoryMethodName(factoryMethodName);
			}
		}
	}
	
	public void parseConstructorArgElements(Element ele, AbstractBeanDefinition bd) {
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT)) {
				parseConstructorArgElement((Element)node, bd);
			}
		}
	}
	
	public void parseConstructorArgElement(Element ele, AbstractBeanDefinition bd) {
		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
		
		if (StringUtils.hasText(indexAttr)) {
			// 指定了index
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					this.readerContext.error("构造器标签的 index 属性不能是负数", ele);
				} else {
					
					Object value = parsePropertyValue(ele, bd, null);
					ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
					if (StringUtils.hasText(typeAttr)) {
						valueHolder.setType(typeAttr);
					}
					if (StringUtils.hasText(nameAttr)) {
						valueHolder.setName(nameAttr);
					}
					valueHolder.setSource(valueHolder);
					if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
						this.readerContext.error("<constructor-arg>的index重复", ele);
					} else {
						bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
					}
				}
			} catch (NumberFormatException e) {
				this.readerContext.error("构造器标签的 index 属性必须是数字", ele, e);
			}
		} else {
			// 没有指定index
			Object value = parsePropertyValue(ele, bd, null);
			ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
			if (StringUtils.hasText(typeAttr)) {
				valueHolder.setType(typeAttr);
			}
			if (StringUtils.hasText(nameAttr)) {
				valueHolder.setName(nameAttr);
			}
			bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
		}
	}
	
	public void parsePropertyElements(Element ele, AbstractBeanDefinition bd) {
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && nodeNameEquals(node, PROPERTY_ELEMENT)) {
				parsePropertyElement((Element)node, bd);
			}
		}
	}
	
	public void parsePropertyElement(Element ele, BeanDefinition bd) {
		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasText(propertyName)) {
			this.readerContext.error("<property>必须有name属性", ele);
			return;
		}
		
		if (bd.getPropertyValues().contains(propertyName)) {
			this.readerContext.error("重复定义的property", ele);
			return;
		}
		
		Object value = parsePropertyValue(ele, bd, propertyName);
		bd.getPropertyValues().add(propertyName, value);
	}
	
	public Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
		String elementName = StringUtils.hasText(propertyName) ? 
				"<property>" + propertyName : "<constructor-arg>";
		
		boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
		
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				if (subElement != null) {
					this.readerContext.error(elementName + "不能包含超过一个的子标签", ele);
				} else {
					subElement = (Element) n;
				}
			}
		}
		
		if ((hasRefAttribute && hasValueAttribute) ||
			((hasRefAttribute || hasValueAttribute) && subElement != null)) {
			this.readerContext.error(elementName + "只能有ref属性、value属性、子标签其中的一个", ele);
		}
		
		if (hasRefAttribute) {
			String refName = ele.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				this.readerContext.error(elementName + "的ref属性是空的", ele);
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName);
			return ref;
		} else if (hasValueAttribute) {
			String value = ele.getAttribute(VALUE_ATTRIBUTE);
			TypedStringValue valueHolder = new TypedStringValue(value);
			valueHolder.setSource(ele);
			return valueHolder;
		} else if (subElement != null) {
			return parsePropertySubElement(subElement, bd);
		} else {
			this.readerContext.error(elementName + "需要指定ref属性或者value属性", ele);
			return null;
		}
	}
	
	public Object parsePropertySubElement(Element ele, BeanDefinition bd) {
		return parsePropertySubElement(ele, bd, null);
	}
	
	public Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultValueType) {
		if (isDefaultNamespace(getNamespaceUri(ele))) {
			
			if (nodeNameEquals(ele, BEAN_ELEMENT)) {
				return parseBeanDefinitionElement(ele, bd);
			} else if (nodeNameEquals(ele, LIST_ELEMENT)) {
				return parseListElement(ele, bd);
			} else if (nodeNameEquals(ele, SET_ELEMENT)) {
				return parseSetElement(ele, bd);
			} else if (nodeNameEquals(ele, MAP_ELEMENT)) {
				return parseMapElement(ele, bd);
			} else if (nodeNameEquals(ele, VALUE_ELEMENT)) {
				return parseValueElement(ele, defaultValueType);
			}
		}
		this.readerContext.error("不明子标签配置", ele);
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public List parseListElement(Element collectionEle, BeanDefinition bd) {
		String defaultValueType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		
		NodeList nl = collectionEle.getChildNodes();
		List target = new ArrayList(nl.getLength());
		parseCollectionElements(nl, target, bd, defaultValueType);
		return target;
	}

	@SuppressWarnings("rawtypes")
	public Object parseSetElement(Element collectionEle, BeanDefinition bd) {
		String defaultValueType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		
		NodeList nl = collectionEle.getChildNodes();
		Set target = new LinkedHashSet(nl.getLength());
		parseCollectionElements(nl, target, bd, defaultValueType);
		return target;
	}

	public Object parseMapElement(Element mapEle, BeanDefinition bd) {
		// map仅支持entry子标签，entry有必输属性key
		NodeList nl = mapEle.getChildNodes();
		Map<String, Object> target = new HashMap<String, Object>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element && nodeNameEquals(n, ENTRY_ELEMENT)) {
				Element entry = (Element) n;
				String key = entry.getAttribute(KEY_TYPE_ATTRIBUTE);
				
				NodeList subNl = entry.getChildNodes();
				if (subNl.getLength() > 1) {
					this.readerContext.error("<entry>只能有一个子元素", mapEle);
				}
				
				target.put(key, parsePropertySubElement((Element) subNl.item(0), bd));
			}
		}
		
		return target;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void parseCollectionElements(NodeList nodeList, Collection target, BeanDefinition bd, String defaultType) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n instanceof Element) {
				target.add(parsePropertySubElement((Element) n, bd));
			}
		}
		
	}
	
	public Object parseValueElement(Element valEle, String defaultValueType) {
		String value = getTextValue(valEle);
		String typeName = defaultValueType;
		
		try {
			TypedStringValue typedValue = buildTypedStringValue(value, typeName);
			typedValue.setTargetType(typeName);
			typedValue.setSource(valEle);
			
			return typedValue;
		} catch (ClassNotFoundException e) {
			this.readerContext.error("对于<value>子标签，未找到指定的类型" + typeName, valEle, e);
			return value;
		}
	}
	
	public String getTextValue(Element valEle) {
		StringBuilder sb = new StringBuilder();
		NodeList nl = valEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if ((item instanceof CharacterData && !(item instanceof Comment)) || item instanceof EntityReference) {
				sb.append(item.getNodeValue());
			}
		}
		return sb.toString();
	}
	
	public String generateBeanName(BeanDefinition bd, boolean isInnerBean) {
		String generatedBeanName = bd.getBeanClassName();
		if (!StringUtils.hasText(generatedBeanName)) {
			if (StringUtils.hasText(bd.getFactoryBeanName())) {
				generatedBeanName = bd.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			this.readerContext.error("请指定<bean>的class属性或者factory-bean属性", null);
		}
		
		String beanName = generatedBeanName;
		if (isInnerBean) {
			beanName = beanName + GENERATED_BEAN_NAME_SEPARATOR + Integer.toHexString(System.identityHashCode(bd));
		} else {
			// 以class为name的bean，可能有多个，所以加上号码加以区别
			int counter = -1;
			while (this.readerContext.getRegistry().containsBeanDefinition(beanName)) {
				counter++;
				beanName = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return beanName;
	}
	
	protected TypedStringValue buildTypedStringValue(String value, String typeName) 
			throws ClassNotFoundException {
		
		ClassLoader classLoader = this.readerContext.getReader().getClassLoader();
		TypedStringValue typedValue;
		
		if (!StringUtils.hasText(typeName)) {
			typedValue = new TypedStringValue(value);
		} else if (classLoader != null) {
			Class<?> type = ClassUtils.forName(typeName, classLoader);
			typedValue = new TypedStringValue(value, type);
		} else {
			typedValue = new TypedStringValue(value, typeName);
		}
		return typedValue;
	}
	
	protected void checkBeanNameUniqueness(String beanName, Element ele) {
		if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
			this.readerContext.error("Bean Name " + beanName + " 已经被使用", ele);
		}
		this.usedNames.add(beanName);
	}
	
	private int getAutowireMode(String autowire) {
		int autowireMode = AbstractBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BYNAME_VALUE.equals(autowire)) {
			autowireMode = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
		}
		if (AUTOWIRE_BYTYPE_VALUE.equals(autowire)) {
			autowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
		}
		return autowireMode;
	}
}
