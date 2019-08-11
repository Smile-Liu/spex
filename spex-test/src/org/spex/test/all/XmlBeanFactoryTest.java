package org.spex.test.all;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.xml.XmlBeanFactory;

public class XmlBeanFactoryTest {


	public static void main(String[] args) throws Exception {
		BeanFactory factory = new XmlBeanFactory("config/spex-all.xml");
		TestBean mouse = (TestBean) factory.getBean("family");
		System.out.println(mouse);
	}
}
