package org.spex.test.beanInstantiation;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.xml.XmlBeanFactory;

public class BeanInstantiationTest {

	private String a;
	
	public BeanInstantiationTest() {
		System.out.println("�޲ι�����");
	}
	
	public BeanInstantiationTest(String a, BeanInstantiationTest b) {
		this.a = a;
		System.out.println("�вι����� a=" + a + " b=" + b.toString());
	}
	
	public String toString() {
		if (this.a == null) {
			return "����һ���޲ι�����ʵ������Bean";
		} else {
			return "BeanInstantiationTest : " + a;
		}
	}
	
	public static void main(String[] args) {
		BeanFactory factory = new XmlBeanFactory("config/spex-bean_instant.xml");
//		factory.getBean("beanNonArgs");
//		factory.getBean("beanOneArg");
		factory.getBean("beanTwoArgs");
	}
}
