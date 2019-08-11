package org.spex.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spex.beans.BeanWrapper;
import org.spex.beans.BeanWrapperImpl;
import org.spex.beans.TypeConverter;
import org.spex.beans.factory.BeanCreationException;
import org.spex.beans.factory.BeanDefinitionStoreException;
import org.spex.beans.factory.config.AbstractBeanDefinition;
import org.spex.beans.factory.config.ConstructorArgumentValues;
import org.spex.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.spex.beans.factory.config.TypedStringValue;
import org.spex.core.GenericTypeResolver;
import org.spex.core.MethodParameter;
import org.spex.core.ParameterNameDiscoverer;
import org.spex.util.ClassUtils;
import org.spex.util.MethodInvoker;
import org.spex.util.ReflectionUtils;
import org.spex.util.StringUtils;

public class ConstructorResolver {

	private final DefaultListableBeanFactory beanFactory;
	
	public ConstructorResolver(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	
	/**
	 * 通过构造器来实例化bean，并放入包装器
	 * @param beanName
	 * @param mbd
	 * @param ctors
	 * @param args
	 * @return
	 */
	protected BeanWrapper autowireConstructor(final String beanName, final RootBeanDefinition mbd, 
			final Constructor<?>[] chosenCtors, final Object[] explicitArgs) {
		
		BeanWrapperImpl bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);
		
		// 首先确认要使用的构造器
		// 其次确认参数
		// 最后反射来进行实例化
		
		Constructor<?> constructorToUse = null;
		Object[] argsToUse = null;
		
		if (explicitArgs != null) {
			// 如果指定了参数，就使用指定的参数
			argsToUse = explicitArgs;
		} else {
			// 没指定参数，则查看是否之前有处理过该BeanDefinition
			constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
			if (constructorToUse != null) {
				argsToUse = mbd.resolvedConstructorArguments;
				if (argsToUse == null) {
					argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse);
				}
			}
		}
		
		if (constructorToUse == null) {
			// 未找到可用的构造器，则继续查找
			// 是否是构造器注入
			boolean autowiring = chosenCtors != null || mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
			
			ConstructorArgumentValues resolvedValues = null;
			
			// 处理配置的参数，获得参数数量
			int minNumberOfArgs;
			if (explicitArgs != null) {
				minNumberOfArgs = explicitArgs.length;
			} else {
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				resolvedValues = new ConstructorArgumentValues();
				minNumberOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}
			
			// 获得可用的构造器
			Constructor<?>[] candidates = chosenCtors;
			if (candidates == null) {
				Class<?> beanClass = mbd.getBeanClass();
				
				try {
					candidates = mbd.isLenientConstructorSolution() ? 
							beanClass.getDeclaredConstructors() : beanClass.getConstructors();
				} catch (Throwable e) {
					throw new BeanCreationException(beanName, "类Class：" + beanClass.getName() + "获取构造器来实例化Bean失败", e);
				}
			}
			AutowireUtils.sortConstructor(candidates);
			
			int minTypeDiffWeight = Integer.MAX_VALUE;
			Set<Constructor<?>> ambiguousConstructors = null;
			List<Exception> causes = null;
			
			for (int i = 0; i < candidates.length; i++) {
				Constructor<?> candidate = candidates[i];
				Class<?>[] paramTypes = candidate.getParameterTypes();
				
				if (constructorToUse != null && argsToUse.length > paramTypes.length) {
					// 已经找到合适的构造器
					break;
				}
				if (paramTypes.length < minNumberOfArgs) {
					// 指定的参数个数只能少不能多
					throw new BeanCreationException(beanName, "指定了 " + minNumberOfArgs + " 个参数，但是没有匹配的构造器");
				}
				
				// 开始解析参数
				ArgumentsHolder args;
				if (resolvedValues != null) {
					// 获取参数名称
					String[] paramNames = null;
					
					ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
					if (pnd != null) {
						paramNames = pnd.getParameterNames(candidate);
					}
					
					try {
						args = createArgumentArray(beanName, mbd, resolvedValues, bw, 
								paramTypes, paramNames, candidate, autowiring);
					} catch (BeanCreationException e) {
						if (i == candidates.length - 1 && constructorToUse == null) {
							if (causes != null) {
								for (Exception cause : causes) {
									this.beanFactory.onSuppressedException(cause);
								}
							}
							throw e;
						}
						if (causes == null) {
							causes = new LinkedList<Exception>();
						}
						causes.add(e);
						continue;
					}
				} else {
					// 指定的参数个数要和构造器的参数个数一致
					if (paramTypes.length != explicitArgs.length) {
						continue;
					}
					args = new ArgumentsHolder(explicitArgs);
				}
				
				// 权重来选择构造器
				int typeDiffWeight = mbd.isLenientConstructorSolution() ?
						args.getTypeDifferenceWeight(paramTypes) : args.getAssignabilityWeight(paramTypes);
				if (typeDiffWeight < minTypeDiffWeight) {
					constructorToUse = candidate;
					argsToUse = args.arguments;
					minTypeDiffWeight = typeDiffWeight;
					ambiguousConstructors = null;
					
				} else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
					if (ambiguousConstructors == null) {
						ambiguousConstructors = new HashSet<Constructor<?>>();
						ambiguousConstructors.add(constructorToUse);
					}
					ambiguousConstructors.add(candidate);
				}
			}
			
			if (constructorToUse == null) {
				throw new BeanCreationException(beanName, "找不到合适的构造器");
			}
			if (ambiguousConstructors != null && !mbd.isLenientConstructorSolution()) {
				throw new BeanCreationException(beanName, "找到多个不确定的构造器：" + ambiguousConstructors);
			}
			
			if (explicitArgs == null) {
				mbd.resolvedConstructorOrFactoryMethod = constructorToUse;
			}
			
			// 反射生成实例
			Object beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, 
					this.beanFactory, constructorToUse, argsToUse);
			bw.setWrappedInstance(beanInstance);
			return bw;
		}
		
		return bw;
	}
	
	
	/**
	 * 通过工厂方法来实例化bean，并放入包装器
	 * @param beanName
	 * @param mbd
	 * @param explicitArgs
	 * @return
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {
		BeanWrapperImpl bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);
		
		// 指定了factory-method属性，则class属性变得无意义
		
		Object factoryBean = null;
		Class<?> factoryClass = null;
		boolean isStatic = false;
		
		// 处理factory bean属性
		String factoryBeanName = mbd.getFactoryBeanName();
		if (factoryBeanName != null) {
			// 有factory-bean属性，则使用factory-bean指定的类来生成bean
			// 如果factoryBeanName和beanName重名，则表示循环生成，不可以
			if (beanName.equals(factoryBeanName)) {
				throw new BeanDefinitionStoreException(beanName + "配置项中factory-bean指向了自己，不支持循环生成");
			}
			
			// factory-bean和FactoryBean不一样，只是一个普通的类
			factoryBean = this.beanFactory.getBean(factoryBeanName);
			if (factoryBean == null) {
				throw new BeanCreationException(beanName, "配置项的factory-bean生成失败，返回空");
			}
			
			factoryClass = factoryBean.getClass();
			isStatic = false;
		} else {
			// 没有指定factory-bean，则表示工厂方法是静态的
			// 使用class作为factory-bean
			if (!mbd.hasBeanClass()) {
				throw new BeanDefinitionStoreException(beanName + " 未配置 class 或 factory-bean");
			}
			
			factoryClass = mbd.getBeanClass();
			isStatic = true;
		}
		
		// 处理factory method属性
		Method factoryMethod = null;
		Object[] argsForFactoryMethod = null;
		
		if (explicitArgs != null) {
			argsForFactoryMethod = explicitArgs;
		} else {
			factoryMethod = (Method) mbd.resolvedConstructorOrFactoryMethod;
			if (factoryMethod != null) {
				argsForFactoryMethod = mbd.resolvedConstructorArguments;
				
				// 未指定参数，则使用内置参数
				if (argsForFactoryMethod == null && mbd.preparedConstructorArguments != null) {
					argsForFactoryMethod = resolvePreparedArguments(beanName, mbd, bw, factoryMethod);
				}
			}
		}
		
		// 没有处理过的缓存的工厂方法，则去遍历类中的所有方法
		if (factoryMethod == null || argsForFactoryMethod == null) {
			
			// 找到factoryClass下的所有方法
			final Class<?> factoryClazz = factoryClass;
			Method[] rawCandidates = mbd.isNonPublicAccessAllowed() ? 
				ReflectionUtils.getAllDeclaredMethod(factoryClazz) : factoryClazz.getMethods();
			
			// 找到和指定factoryMethod同名、同静态修饰符的方法（可能有重构）
			List<Method> candidateList = new ArrayList<Method>();
			for (Method candidate : rawCandidates) {
				if (candidate != null && Modifier.isStatic(candidate.getModifiers()) == isStatic &&
						candidate.getName().equals(mbd.getFactoryMethodName())) {
					candidateList.add(candidate);
				}
			}
			
			Method[] candidates = candidateList.toArray(new Method[candidateList.size()]);
			AutowireUtils.sortFactoryMethod(candidates);
			
			// 参数转换
			ConstructorArgumentValues resolvedValues = null;
			int minTypeDiffWeight = Integer.MAX_VALUE;
			boolean autowiring = mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
			Set<Method> ambiguousFactoryMethods = null;
			
			int minNumberOfArgs;
			if (explicitArgs != null) {
				minNumberOfArgs = explicitArgs.length;
			} else {
				// 使用constructor-arg子标签的值
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				
				resolvedValues = new ConstructorArgumentValues();
				// 填充内容
				minNumberOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}
			
			List<Exception> causes = null;
			
			// 遍历候选方法数组
			for (int i = 0; i < candidates.length; i++) {
				Method candidate = candidates[i];
				Class<?>[] paramTypes = candidate.getParameterTypes();
				
				// 需要的参数个数 大于等于 提供的参数个数（传入的、constructor-arg标签指定的）
				if (paramTypes.length >= minNumberOfArgs) {
					ArgumentsHolder args = null;
					
					if (resolvedValues != null) {
						// 获取参数名称
						String[] parameterNames = null;
						ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
						if (pnd != null) {
							parameterNames = pnd.getParameterNames(candidate);
						}
						
						// 遍历每个候选方法的形参去找配置项的值，组成数组
						// createArgumentArray要求：只有在 构造器注入 时，才允许方法参数个数和指定参数个数不一致
						try {
							args = createArgumentArray(
									beanName, mbd, resolvedValues, bw, paramTypes, parameterNames, candidate, autowiring);
						} catch (BeanCreationException e) {
							if (i == candidates.length - 1 && factoryMethod == null) {
								if (causes != null) {
									for (Exception cause : causes) {
										this.beanFactory.onSuppressedException(cause);
									}
								}
								throw e;
							} else {
								if (causes == null) {
									causes = new LinkedList<Exception>();
								}
								causes.add(e);
								continue;
							}
						}
					} else {
						if (paramTypes.length != explicitArgs.length) {
							continue;
						}
						args = new ArgumentsHolder(explicitArgs);
					}
					
					// 根据权重来选择工厂方法
					int typeDiffWeight = mbd.isLenientConstructorSolution() ?
							args.getTypeDifferenceWeight(paramTypes) : args.getAssignabilityWeight(paramTypes);
					if (typeDiffWeight < minTypeDiffWeight) {
						factoryMethod = candidate;
						argsForFactoryMethod = args.arguments;
						minTypeDiffWeight = typeDiffWeight;
						ambiguousFactoryMethods = null;
					} else if (factoryMethod != null && typeDiffWeight == minTypeDiffWeight) {
						if (ambiguousFactoryMethods == null) {
							ambiguousFactoryMethods = new LinkedHashSet<Method>();
							ambiguousFactoryMethods.add(factoryMethod);
						}
						ambiguousFactoryMethods.add(candidate);
					}
				}
			}
			
			if (factoryMethod == null) {
				// 报错
				boolean hasArgs = resolvedValues.getArgumentCount() > 0;
				String argsDesc = "";
				if (hasArgs) {
					List<String> argTypes = new ArrayList<String>();
					for (ValueHolder valueHolder : resolvedValues.getIndexedArgumentValues().values()) {
						String argType = valueHolder.getType() != null ?
								ClassUtils.getShortName(valueHolder.getType()) : valueHolder.getValue().getClass().getSimpleName();
						argTypes.add(argType);
					}
					argsDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
				}
				
				throw new BeanCreationException(beanName, "未找到配置的factory-method：" +
						(mbd.getFactoryBeanName() != null ? "factory-bean '" + mbd.getFactoryBeanName() + "'" : "") +
						"factory-method '" + mbd.getFactoryMethodName() + "(" + argsDesc+ ")'。请检查方法名" + 
						(hasArgs ? "和参数" : "") + "是否正确，以及是否是" + (isStatic ? "static" : "non-static"));
			} else if (void.class.equals(factoryMethod.getReturnType())) {
				// 工厂方法没有返回值的话，报错
				throw new BeanCreationException(beanName, "非法factory-method：不能返回void");
			} else if (ambiguousFactoryMethods != null && !mbd.isLenientConstructorSolution()) {
				// 有不确定且模糊不清的工厂方法时，报错
				throw new BeanCreationException(beanName, "在bean中找到不确定的工厂方法，" + ambiguousFactoryMethods);
			}
			
			if (explicitArgs == null) {
				mbd.resolvedConstructorOrFactoryMethod = factoryMethod;
			}
		}
		
		// 执行工厂方法，得到bean
		try {
			Object beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, 
					this.beanFactory, factoryBean, factoryMethod, argsForFactoryMethod);
			
			if (beanInstance == null) {
				return null;
			}
			bw.setWrappedInstance(beanInstance);
			return bw;
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "通过factory-method实例化失败", e);
		}
	}
	
	private ArgumentsHolder createArgumentArray(String beanName, RootBeanDefinition mbd, 
			ConstructorArgumentValues resolvedValues, BeanWrapper bw, Class<?>[] paramTypes, String[] paramNames,
			Object methodOrCtor, boolean autowiring) {
		
		String methodType = methodOrCtor instanceof Constructor ? "constructor" : "factory-method";
		TypeConverter converter = bw;
		
		ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
		
		Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = 
			new HashSet<ConstructorArgumentValues.ValueHolder>(paramTypes.length);
		Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
		boolean resolveNecessary = false;
		
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			String paramName = paramNames != null ? paramNames[i] : null;
			
			// 找到对应索引或对应类型的构造器参数
			ConstructorArgumentValues.ValueHolder valueHolder = 
				resolvedValues.getArgumentValue(i, paramType, paramName, usedValueHolders);
			
			// 如果未找到构造器参数，并且不支持构造器注入，则尝试下一个参数
			if (valueHolder == null && !autowiring) {
				valueHolder = resolvedValues.getGenericArgumentValue(null, null, usedValueHolders);
			}
			
			if (valueHolder != null) {
				// 不要重复处理
				usedValueHolders.add(valueHolder);
				
				ConstructorArgumentValues.ValueHolder sourceHolder = 
					(ValueHolder) valueHolder.getSource();
				Object originalValue = valueHolder.getValue();
				Object sourceValue = sourceHolder.getValue();
				
				Object convertedValue;
				
				if (valueHolder.isConverted()) {
					convertedValue = valueHolder.getConvertedValue();
					args.preparedArguments[i] = convertedValue;
				} else {
					convertedValue = converter.convertIfNecessary(originalValue, paramType, 
							MethodParameter.forMethodOrConstructor(methodOrCtor, i));
					
					if (originalValue == sourceValue || sourceValue instanceof TypedStringValue) {
						sourceHolder.setConvertedValue(convertedValue);
						args.preparedArguments[i] = convertedValue;
					} else {
						resolveNecessary = true;
						args.preparedArguments[i] = sourceValue;
					}
				}
				args.arguments[i] = convertedValue;
				args.rawArguments[i] = originalValue;
				
			} else {
				if (!autowiring) {
					throw new BeanCreationException(beanName, 
							"指定参数 index:" + i + " type:" + ClassUtils.getQualifiedName(paramType) + " 不符合，" +
							"请确认 " + methodType + " 参数类型");
				}
				
				MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, i);
				Object autowiredArgument = resolveAutowiredArgument(methodParam, beanName, autowiredBeanNames, converter);
				args.rawArguments[i] = autowiredArgument;
				args.arguments[i] = autowiredArgument;
				args.preparedArguments[i] = autowiredArgument;
				resolveNecessary = true;
			}
		}
		
		for (String autowiredBeanName : autowiredBeanNames) {
			this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
		}
		
		if (resolveNecessary) {
			mbd.preparedConstructorArguments = args.preparedArguments;
		} else {
			mbd.resolvedConstructorArguments = args.arguments;
		}
		
		mbd.constructorArgumentsResolved = true;
		return args;
	}
	
	protected Object resolveAutowiredArgument(
			MethodParameter param, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) {
		
		return null;
	}
	
	public void resolveFactoryMethodIfPossible(RootBeanDefinition mbd) {
		Class<?> factoryClass;
		if (mbd.getFactoryBeanName() != null) {
			factoryClass = this.beanFactory.getType(mbd.getFactoryBeanName());
		} else {
			factoryClass = mbd.getBeanClass();
		}
		
		factoryClass = ClassUtils.getUserClass(factoryClass);
		Method[] candidates = ReflectionUtils.getAllDeclaredMethod(factoryClass);
		Method uniqueCandidate = null;
		for (Method candidate : candidates) {
			if (mbd.isFactoryMethod(candidate)) {
				if (uniqueCandidate == null) {
					uniqueCandidate = candidate;
				} else if (!Arrays.equals(uniqueCandidate.getParameterTypes(), candidate.getParameterTypes())) {
					uniqueCandidate = null;
					break;
				}
			}
		}
		mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
	}
	
	/**
	 * 处理preparedArgument，解析成可以使用的参数
	 * @param beanName
	 * @param mbd
	 * @param bw
	 * @param methodOrCtor
	 * @return
	 */
	private Object[] resolvePreparedArguments(String beanName, RootBeanDefinition mbd, BeanWrapper bw, Member methodOrCtor) {
		
		Class<?>[] paramTypes = (methodOrCtor instanceof Method) ?
				((Method) methodOrCtor).getParameterTypes() : ((Constructor<?>) methodOrCtor).getParameterTypes();

		Object[] argsToResolve = mbd.preparedConstructorArguments;
		
		TypeConverter converter = this.beanFactory.getCustomTypeConverter() != null ?
				this.beanFactory.getCustomTypeConverter() : bw;
				
//		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
		
		Object[] resultArgs = new Object[argsToResolve.length];
		
		for (int i = 0; i < argsToResolve.length; i++) {
			Object argValue = argsToResolve[i];
			
			MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, i);
			GenericTypeResolver.resolveParameterType(methodParam, methodOrCtor.getDeclaringClass());
			
			if (argValue instanceof String) {
				argValue = this.beanFactory.evaluateBeanDefinitionString((String) argValue, mbd);
			}
			
			Class<?> paramType = paramTypes[i];
			
			try {
				resultArgs[i] = converter.convertIfNecessary(argValue, paramType, methodParam);
			} catch (Throwable e) {
				String methodType = methodOrCtor instanceof Method ? "factory method" : "constructor";
				
				throw new BeanCreationException(beanName, "不能转换 " + methodType + "的参数值：" + 
						(argValue != null ? argValue.getClass().getName() : null) + 
						" 参数位置 " + i + " 要求类型 " + paramType.getName());
			}
				
		}
		return resultArgs;
	}
	
	private int resolveConstructorArguments(String beanName, RootBeanDefinition mbd, BeanWrapper bw, 
			ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {
		
		TypeConverter converter = bw;
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
		
		int minNumberOfArgs = cargs.getArgumentCount();
		
		for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : cargs.getIndexedArgumentValues().entrySet()) {
			int index = entry.getKey();
			if (index > minNumberOfArgs) {
				minNumberOfArgs = index + 1;
			}
			
			ConstructorArgumentValues.ValueHolder valueHolder = entry.getValue();
			if (valueHolder.isConverted()) {
				resolvedValues.addIndexedArgumentValue(index, valueHolder);
			} else {
				
				// 将ValueHolder中的value转换value对应type的值（不是valueHolder的type）
				Object resolvedValue = valueResolver.resolveValueIfNecessary("构造器参数", valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder = 
					new ConstructorArgumentValues.ValueHolder(valueHolder.getName(), resolvedValue, valueHolder.getType());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
			}
		}
		
		for (ConstructorArgumentValues.ValueHolder valueHolder : cargs.getGenericArgumentValues()) {
			if (valueHolder.isConverted()) {
				resolvedValues.addGenericArgumentValue(valueHolder);
			} else {
				// 将ValueHolder中的value转换value对应type的值（不是valueHolder的type）
				Object resolvedValue = valueResolver.resolveValueIfNecessary("构造器参数", valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder = 
					new ConstructorArgumentValues.ValueHolder(valueHolder.getName(), resolvedValue, valueHolder.getType());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addGenericArgumentValue(resolvedValueHolder);
			}
		}
		return minNumberOfArgs;
	}



	private static class ArgumentsHolder {
		
		public Object[] rawArguments;
		public Object[] arguments;
		public Object[] preparedArguments;
		
		public ArgumentsHolder(int size) {
			rawArguments = new Object[size];
			arguments = new Object[size];
			preparedArguments = new Object[size];
		}
		
		public ArgumentsHolder(Object[] args) {
			rawArguments = args;
			arguments = args;
			preparedArguments = args;
		}
		
		public int getTypeDifferenceWeight(Class<?>[] paramTypes) {
			int typeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.arguments);
			int rawTypeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
			return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
		}
		
		public int getAssignabilityWeight(Class<?>[] paramTypes) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (!ClassUtils.isAssignableValue(paramTypes[i], this.arguments[i])) {
					return Integer.MAX_VALUE;
				}
			}
			
			for (int i = 0; i < paramTypes.length; i++) {
				if (!ClassUtils.isAssignableValue(paramTypes[i], this.rawArguments[i])) {
					return Integer.MAX_VALUE - 512;
				}
			}
			
			return Integer.MAX_VALUE - 1024;
		}
	}
}
