package org.spex.test.propertydescriptor;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.spex.core.BridgeMethodResolver;
import org.spex.util.ClassUtils;

public class CatBean implements BridgeMethodInterface<Object> {

	private int age;
	
	private String name;

	private String address; // 不是属性Property，只是字段Field
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	@Override
	public void setName(Object name) {
		
	}

//	
//	public void setName(Class<?>[] names) {
//		this.name = names[0].getName();
//	}
	
	public static void makeWriteMethod() throws IntrospectionException {
		PropertyDescriptor[] pds = Introspector.getBeanInfo(CatBean.class).getPropertyDescriptors();
//		Method nameSetter = ClassUtils.getMethodIfAvailable(CatBean.class, "setName", String.class);
//		System.out.println(nameSetter);
		
		System.out.println("raw method:" + pds[2].getWriteMethod());
		System.out.println(pds[2].getWriteMethod().isBridge());
		System.out.println("bridge method:" + BridgeMethodResolver.findBridgedMethod(pds[2].getWriteMethod()));
	}
	
	public static void main(String[] args) throws IntrospectionException {
		makeWriteMethod();
		
//		PropertyDescriptor pds = new PropertyDescriptor("name", CatBean.class);
//		System.out.println(pds.getPropertyType());
//		System.out.println(pds.getPropertyEditorClass());
//		System.out.println(pds.getReadMethod());
		
		
//		System.out.println(pds.getWriteMethod());
		
//		Method method = pds.getWriteMethod();
//		System.out.println(method.getDeclaringClass().getName());
		
//		BeanInfo beanInfo = Introspector.getBeanInfo(CatBean.class);
//		System.out.println(Arrays.toString(beanInfo.getPropertyDescriptors()));
		
//		List<String> list = new ArrayList<String>();
//		System.out.println(list.getClass().getGenericSuperclass());
//		System.out.println(((ParameterizedType)list.getClass().getGenericSuperclass()).getRawType());
//		System.out.println(((Class)((ParameterizedType)list.getClass().getGenericSuperclass()).getRawType()).getTypeParameters()[0]);
//		System.out.println(((ParameterizedType)list.getClass().getGenericSuperclass()).getOwnerType());
//		System.out.println(((ParameterizedType)list.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
//		System.out.println(list.getClass().getGenericSuperclass() instanceof ParameterizedType);
		
//		List<String> list = new ArrayList<String>();
//		System.out.println(list.getClass().getGenericInterfaces()[0]);
//		System.out.println(((ParameterizedType)list.getClass().getGenericInterfaces()[0]).getRawType());
//		System.out.println(((Class)((ParameterizedType)list.getClass().getGenericInterfaces()[0]).getRawType()).getTypeParameters()[0]);
//		System.out.println(((ParameterizedType)list.getClass().getGenericInterfaces()[0]).getOwnerType());
//		System.out.println(((ParameterizedType)list.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]);
//		System.out.println(list.getClass().getGenericInterfaces()[0] instanceof ParameterizedType);
		
//		Method[] ms = CatBean.class.getDeclaredMethods();
//		printArray(ms);
//		Method wm = ms[ms.length - 2];
//		Type[] genericTypes = wm.getGenericParameterTypes();
//		System.out.println(genericTypes[0] instanceof GenericArrayType);
//		printArray(genericTypes);
//		Class[] paramTypes = wm.getParameterTypes();
//		System.out.println(paramTypes[0].getComponentType());
//		
//		Type component = ((GenericArrayType)genericTypes[0]).getGenericComponentType();
		
//		System.out.println(component);
//		System.out.println(component instanceof ParameterizedType);
		
//		component = ((ParameterizedType) component).getRawType();
//		System.out.println(paramTypes[0].getComponentType().equals(component));
//		printArray(paramTypes);
	}
	
	private static void printArray(Object[] arr) {
		for (Object obj : arr) {
			System.out.println(obj.toString());
		}
		System.out.println("-----------------------------------");
	}

}
