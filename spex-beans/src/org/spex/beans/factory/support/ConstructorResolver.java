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
	 * ͨ����������ʵ����bean���������װ��
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
		
		// ����ȷ��Ҫʹ�õĹ�����
		// ���ȷ�ϲ���
		// �����������ʵ����
		
		Constructor<?> constructorToUse = null;
		Object[] argsToUse = null;
		
		if (explicitArgs != null) {
			// ���ָ���˲�������ʹ��ָ���Ĳ���
			argsToUse = explicitArgs;
		} else {
			// ûָ����������鿴�Ƿ�֮ǰ�д������BeanDefinition
			constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
			if (constructorToUse != null) {
				argsToUse = mbd.resolvedConstructorArguments;
				if (argsToUse == null) {
					argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse);
				}
			}
		}
		
		if (constructorToUse == null) {
			// δ�ҵ����õĹ����������������
			// �Ƿ��ǹ�����ע��
			boolean autowiring = chosenCtors != null || mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
			
			ConstructorArgumentValues resolvedValues = null;
			
			// �������õĲ�������ò�������
			int minNumberOfArgs;
			if (explicitArgs != null) {
				minNumberOfArgs = explicitArgs.length;
			} else {
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				resolvedValues = new ConstructorArgumentValues();
				minNumberOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}
			
			// ��ÿ��õĹ�����
			Constructor<?>[] candidates = chosenCtors;
			if (candidates == null) {
				Class<?> beanClass = mbd.getBeanClass();
				
				try {
					candidates = mbd.isLenientConstructorSolution() ? 
							beanClass.getDeclaredConstructors() : beanClass.getConstructors();
				} catch (Throwable e) {
					throw new BeanCreationException(beanName, "��Class��" + beanClass.getName() + "��ȡ��������ʵ����Beanʧ��", e);
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
					// �Ѿ��ҵ����ʵĹ�����
					break;
				}
				if (paramTypes.length < minNumberOfArgs) {
					// ָ���Ĳ�������ֻ���ٲ��ܶ�
					throw new BeanCreationException(beanName, "ָ���� " + minNumberOfArgs + " ������������û��ƥ��Ĺ�����");
				}
				
				// ��ʼ��������
				ArgumentsHolder args;
				if (resolvedValues != null) {
					// ��ȡ��������
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
					// ָ���Ĳ�������Ҫ�͹������Ĳ�������һ��
					if (paramTypes.length != explicitArgs.length) {
						continue;
					}
					args = new ArgumentsHolder(explicitArgs);
				}
				
				// Ȩ����ѡ������
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
				throw new BeanCreationException(beanName, "�Ҳ������ʵĹ�����");
			}
			if (ambiguousConstructors != null && !mbd.isLenientConstructorSolution()) {
				throw new BeanCreationException(beanName, "�ҵ������ȷ���Ĺ�������" + ambiguousConstructors);
			}
			
			if (explicitArgs == null) {
				mbd.resolvedConstructorOrFactoryMethod = constructorToUse;
			}
			
			// ��������ʵ��
			Object beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, 
					this.beanFactory, constructorToUse, argsToUse);
			bw.setWrappedInstance(beanInstance);
			return bw;
		}
		
		return bw;
	}
	
	
	/**
	 * ͨ������������ʵ����bean���������װ��
	 * @param beanName
	 * @param mbd
	 * @param explicitArgs
	 * @return
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {
		BeanWrapperImpl bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);
		
		// ָ����factory-method���ԣ���class���Ա��������
		
		Object factoryBean = null;
		Class<?> factoryClass = null;
		boolean isStatic = false;
		
		// ����factory bean����
		String factoryBeanName = mbd.getFactoryBeanName();
		if (factoryBeanName != null) {
			// ��factory-bean���ԣ���ʹ��factory-beanָ������������bean
			// ���factoryBeanName��beanName���������ʾѭ�����ɣ�������
			if (beanName.equals(factoryBeanName)) {
				throw new BeanDefinitionStoreException(beanName + "��������factory-beanָ�����Լ�����֧��ѭ������");
			}
			
			// factory-bean��FactoryBean��һ����ֻ��һ����ͨ����
			factoryBean = this.beanFactory.getBean(factoryBeanName);
			if (factoryBean == null) {
				throw new BeanCreationException(beanName, "�������factory-bean����ʧ�ܣ����ؿ�");
			}
			
			factoryClass = factoryBean.getClass();
			isStatic = false;
		} else {
			// û��ָ��factory-bean�����ʾ���������Ǿ�̬��
			// ʹ��class��Ϊfactory-bean
			if (!mbd.hasBeanClass()) {
				throw new BeanDefinitionStoreException(beanName + " δ���� class �� factory-bean");
			}
			
			factoryClass = mbd.getBeanClass();
			isStatic = true;
		}
		
		// ����factory method����
		Method factoryMethod = null;
		Object[] argsForFactoryMethod = null;
		
		if (explicitArgs != null) {
			argsForFactoryMethod = explicitArgs;
		} else {
			factoryMethod = (Method) mbd.resolvedConstructorOrFactoryMethod;
			if (factoryMethod != null) {
				argsForFactoryMethod = mbd.resolvedConstructorArguments;
				
				// δָ����������ʹ�����ò���
				if (argsForFactoryMethod == null && mbd.preparedConstructorArguments != null) {
					argsForFactoryMethod = resolvePreparedArguments(beanName, mbd, bw, factoryMethod);
				}
			}
		}
		
		// û�д�����Ļ���Ĺ�����������ȥ�������е����з���
		if (factoryMethod == null || argsForFactoryMethod == null) {
			
			// �ҵ�factoryClass�µ����з���
			final Class<?> factoryClazz = factoryClass;
			Method[] rawCandidates = mbd.isNonPublicAccessAllowed() ? 
				ReflectionUtils.getAllDeclaredMethod(factoryClazz) : factoryClazz.getMethods();
			
			// �ҵ���ָ��factoryMethodͬ����ͬ��̬���η��ķ������������ع���
			List<Method> candidateList = new ArrayList<Method>();
			for (Method candidate : rawCandidates) {
				if (candidate != null && Modifier.isStatic(candidate.getModifiers()) == isStatic &&
						candidate.getName().equals(mbd.getFactoryMethodName())) {
					candidateList.add(candidate);
				}
			}
			
			Method[] candidates = candidateList.toArray(new Method[candidateList.size()]);
			AutowireUtils.sortFactoryMethod(candidates);
			
			// ����ת��
			ConstructorArgumentValues resolvedValues = null;
			int minTypeDiffWeight = Integer.MAX_VALUE;
			boolean autowiring = mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
			Set<Method> ambiguousFactoryMethods = null;
			
			int minNumberOfArgs;
			if (explicitArgs != null) {
				minNumberOfArgs = explicitArgs.length;
			} else {
				// ʹ��constructor-arg�ӱ�ǩ��ֵ
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				
				resolvedValues = new ConstructorArgumentValues();
				// �������
				minNumberOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}
			
			List<Exception> causes = null;
			
			// ������ѡ��������
			for (int i = 0; i < candidates.length; i++) {
				Method candidate = candidates[i];
				Class<?>[] paramTypes = candidate.getParameterTypes();
				
				// ��Ҫ�Ĳ������� ���ڵ��� �ṩ�Ĳ�������������ġ�constructor-arg��ǩָ���ģ�
				if (paramTypes.length >= minNumberOfArgs) {
					ArgumentsHolder args = null;
					
					if (resolvedValues != null) {
						// ��ȡ��������
						String[] parameterNames = null;
						ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
						if (pnd != null) {
							parameterNames = pnd.getParameterNames(candidate);
						}
						
						// ����ÿ����ѡ�������β�ȥ���������ֵ���������
						// createArgumentArrayҪ��ֻ���� ������ע�� ʱ��������������������ָ������������һ��
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
					
					// ����Ȩ����ѡ�񹤳�����
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
				// ����
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
				
				throw new BeanCreationException(beanName, "δ�ҵ����õ�factory-method��" +
						(mbd.getFactoryBeanName() != null ? "factory-bean '" + mbd.getFactoryBeanName() + "'" : "") +
						"factory-method '" + mbd.getFactoryMethodName() + "(" + argsDesc+ ")'�����鷽����" + 
						(hasArgs ? "�Ͳ���" : "") + "�Ƿ���ȷ���Լ��Ƿ���" + (isStatic ? "static" : "non-static"));
			} else if (void.class.equals(factoryMethod.getReturnType())) {
				// ��������û�з���ֵ�Ļ�������
				throw new BeanCreationException(beanName, "�Ƿ�factory-method�����ܷ���void");
			} else if (ambiguousFactoryMethods != null && !mbd.isLenientConstructorSolution()) {
				// �в�ȷ����ģ������Ĺ�������ʱ������
				throw new BeanCreationException(beanName, "��bean���ҵ���ȷ���Ĺ���������" + ambiguousFactoryMethods);
			}
			
			if (explicitArgs == null) {
				mbd.resolvedConstructorOrFactoryMethod = factoryMethod;
			}
		}
		
		// ִ�й����������õ�bean
		try {
			Object beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, 
					this.beanFactory, factoryBean, factoryMethod, argsForFactoryMethod);
			
			if (beanInstance == null) {
				return null;
			}
			bw.setWrappedInstance(beanInstance);
			return bw;
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "ͨ��factory-methodʵ����ʧ��", e);
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
			
			// �ҵ���Ӧ�������Ӧ���͵Ĺ���������
			ConstructorArgumentValues.ValueHolder valueHolder = 
				resolvedValues.getArgumentValue(i, paramType, paramName, usedValueHolders);
			
			// ���δ�ҵ����������������Ҳ�֧�ֹ�����ע�룬������һ������
			if (valueHolder == null && !autowiring) {
				valueHolder = resolvedValues.getGenericArgumentValue(null, null, usedValueHolders);
			}
			
			if (valueHolder != null) {
				// ��Ҫ�ظ�����
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
							"ָ������ index:" + i + " type:" + ClassUtils.getQualifiedName(paramType) + " �����ϣ�" +
							"��ȷ�� " + methodType + " ��������");
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
	 * ����preparedArgument�������ɿ���ʹ�õĲ���
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
				
				throw new BeanCreationException(beanName, "����ת�� " + methodType + "�Ĳ���ֵ��" + 
						(argValue != null ? argValue.getClass().getName() : null) + 
						" ����λ�� " + i + " Ҫ������ " + paramType.getName());
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
				
				// ��ValueHolder�е�valueת��value��Ӧtype��ֵ������valueHolder��type��
				Object resolvedValue = valueResolver.resolveValueIfNecessary("����������", valueHolder.getValue());
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
				// ��ValueHolder�е�valueת��value��Ӧtype��ֵ������valueHolder��type��
				Object resolvedValue = valueResolver.resolveValueIfNecessary("����������", valueHolder.getValue());
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
