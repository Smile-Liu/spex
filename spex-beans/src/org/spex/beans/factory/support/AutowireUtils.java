package org.spex.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.spex.beans.factory.ObjectFactory;
import org.spex.util.ClassUtils;

public class AutowireUtils {

	
	public static void sortFactoryMethod(Method[] factoryMethods) {
		Arrays.sort(factoryMethods, new Comparator<Method>() {
			public int compare(Method fm1, Method fm2) {
				// public的为先
				boolean p1 = Modifier.isPublic(fm1.getModifiers());
				boolean p2 = Modifier.isPublic(fm2.getModifiers());
				if (p1 != p2) {
					return p1 ? -1 : 1;
				}
				
				// 参数多的为先
				int c1pl = fm1.getParameterTypes().length;
				int c2pl = fm2.getParameterTypes().length;
				return new Integer(c1pl).compareTo(c2pl) * -1;
			}
		});
	}
	
	public static void sortConstructor(Constructor<?>[] ctors) {
		Arrays.sort(ctors, new Comparator<Constructor<?>>() {

			@Override
			public int compare(Constructor<?> o1, Constructor<?> o2) {
				boolean p1 = Modifier.isPublic(o1.getModifiers());
				boolean p2 = Modifier.isPublic(o2.getModifiers());
				if (p1 != p2) {
					return p1 ? -1 : 1;
				}
				
				int c1pl = o1.getParameterTypes().length;
				int c2pl = o2.getParameterTypes().length;
				return new Integer(c1pl).compareTo(c2pl) * -1;
			}
		});
	}
	
	public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		Method writeMethod = pd.getWriteMethod();
		if (writeMethod == null) {
			return false;
		}
		
		if (!writeMethod.getDeclaringClass().getName().contains("$$")) {
			// 不是cglib的类，OK
			return false;
		}
		
		Class<?> superClass = writeMethod.getDeclaringClass().getSuperclass();
		return !ClassUtils.hasMethod(superClass, writeMethod.getName(), writeMethod.getParameterTypes());
	}
	
	public static boolean isSetterDefinedInterface(PropertyDescriptor pd, Set<Class<?>> interfaces) {
		Method setter = pd.getWriteMethod();
		if (setter != null) {
			Class<?> targetClass = setter.getDeclaringClass();
			for (Class<?> ifc : interfaces) {
				if (ifc.isAssignableFrom(targetClass) &&
						ClassUtils.hasMethod(ifc, setter.getName(), setter.getParameterTypes())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 把给定的自动装配的值转换为指定类型的值
	 * @param autowiringValue 自动装配的值
	 * @param requiredType 指定类型
	 * @return
	 */
	public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
		
		if (autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue) &&
				autowiringValue instanceof Serializable && requiredType.isInterface()) {
			// 如果是序列化的对象，创建代理
			autowiringValue = Proxy.newProxyInstance(requiredType.getClassLoader(), new Class[] {requiredType}, 
					new ObjectFactoryDelegatingInvocationHandler((ObjectFactory<?>) autowiringValue));
		}
		return autowiringValue;
	}
	
	private static class ObjectFactoryDelegatingInvocationHandler implements InvocationHandler, Serializable {

		private static final long serialVersionUID = 1408320077513694471L;

		private ObjectFactory<?> objectFactory;
		
		public ObjectFactoryDelegatingInvocationHandler(ObjectFactory<?> objectFactory) {
			this.objectFactory = objectFactory;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			String methodName = method.getName();
			if (methodName.equals("equals")) {
				// 是equals方法时，判断代理对象是否相同
				return proxy == args[0];
			} else if (methodName.equals("hashCode")) {
				// 使用代理对象的hashCode
				return System.identityHashCode(proxy);
			} else if (methodName.equals("toString")) {
				return this.objectFactory.toString();
			}
			
			// 其他方法，则调用代理方法
			return method.invoke(this.objectFactory.getObject(), args);
		}
		
	}
}
