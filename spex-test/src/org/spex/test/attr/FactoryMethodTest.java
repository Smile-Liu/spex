package org.spex.test.attr;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.xml.XmlBeanFactory;


public class FactoryMethodTest {

//	public String getStr() {
//		return "123";
//	}
//	
//	public static String getStrStatic() {
//		return "static 123";
//	}
//	
	public static void main(String[] args) {
		BeanFactory factory = new XmlBeanFactory("config/spex-factorymethod.xml");
		System.out.println(factory.getBean("factoryBean"));
	}
	

	public FactoryMethodTest() {
		System.out.println("工厂的无参构造器");
	}
	
	public FactoryMethodTest(String msg) {
		System.out.println("工厂的有参构造器" + msg);
	}
	
	public static Bean getObject() {
		return new Bean("123");
	}

	public static Bean getObject(String msg) {
		return new Bean(msg);
	}
	
	public static class Bean {
		public Bean() {
			System.out.println("无参构造函数");
		}
		
		public Bean(String msg) {
			System.out.println("有参构造函数" + msg);
		}
	}
}
