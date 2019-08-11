package org.spex.test;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.xml.XmlBeanFactory;

public class BeanFactoryTest {

	public static void main(String[] args) throws Exception {
//		BeanFactory factory = new XmlBeanFactory("config/spex.xml");
		
		int i = Character.digit('E', 16);
		int j = Character.digit('5', 16);
		
		char m = (char) ((i << 4) + j);
		System.out.println(i + " " + j + " " + m + " " + (i << 4));
	}
}
