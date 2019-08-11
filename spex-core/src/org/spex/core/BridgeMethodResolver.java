package org.spex.core;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.spex.util.ClassUtils;
import org.spex.util.ReflectionUtils;

/**
 * ���ڷ��͵����룬Ϊ�˱�����ǰ���ݣ�����ʱ��������ͣ���͵������Žӷ���
 * @author hp
 *
 */
public class BridgeMethodResolver {

	public static Method findBridgedMethod(Method bridgeMethod) {
		if (bridgeMethod == null || !bridgeMethod.isBridge()) {
			return bridgeMethod;
		}
		
		// ��bridge method
		// ȡ�����ϵ�ȫ�����������ݷ������ƺͲ�����������ȡ�Žӷ�������������
		List<Method> candidateMethods = new ArrayList<Method>();
		Method[] methods = ReflectionUtils.getAllDeclaredMethod(bridgeMethod.getDeclaringClass());
		
		for (Method m : methods) {
			if (isBridgeCandidateFor(m, bridgeMethod)) {
				candidateMethods.add(m);
			}
		}
		
		// ���ֻ��һ����ѡ����������������ķ���
		if (candidateMethods.size() == 1) {
			return candidateMethods.get(0);
		}
		
		// ����
		Method bridgedMethod = searchCandidates(candidateMethods, bridgeMethod);
		return bridgedMethod;
	}
	
	static boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Map<TypeVariable<?>, Type> typeVariableMap) {
		if (isResolvedTypeMatch(candidateMethod, bridgeMethod, typeVariableMap)) {
			return true;
		}
		
		Method method = findGenericDeclaration(bridgeMethod);
		return method != null && isResolvedTypeMatch(method, candidateMethod, typeVariableMap);
	}
	
	private static Method findGenericDeclaration(Method bridgeMethod) {
		// �ڼ̳������ҵ������Žӷ����ķ���
		Class<?> superClass = bridgeMethod.getDeclaringClass().getSuperclass();
		while (!Object.class.equals(superClass)) {
			Method method = searchForMethod(superClass, bridgeMethod);
			if (method != null && !method.isBridge()) {
				return method;
			}
			superClass = superClass.getSuperclass();
		}
		
		// �ӿ�
		Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(bridgeMethod.getDeclaringClass());
		for (Class<?> ifc : interfaces) {
			Method method = searchForMethod(ifc, bridgeMethod);
			if (method != null && !method.isBridge()) {
				return method;
			}
		}
		
		return null;
	}
	
	private static Method searchForMethod(Class<?> type, Method bridgeMethod) {
		return ReflectionUtils.findMethod(type, bridgeMethod.getName(), bridgeMethod.getParameterTypes());
	}
	
	private static Method searchCandidates(List<Method> candidateMethods, Method bridgeMethod) {
		if (candidateMethods.isEmpty()) {
			return null;
		}
		
		Map<TypeVariable<?>, Type> typeVariableMap = GenericTypeResolver.getTypeVariableMap(bridgeMethod.getDeclaringClass());
		
		Method previousMethod = null;
		boolean sameFlag = true;
		for (Method candidateMethod : candidateMethods) {
			if (isBridgeMethodFor(bridgeMethod, candidateMethod, typeVariableMap)) {
				return candidateMethod;
			} else if (previousMethod != null) {
				sameFlag = sameFlag &&
					Arrays.equals(candidateMethod.getGenericParameterTypes(), previousMethod.getGenericParameterTypes());
			}
			previousMethod = candidateMethod;
		}
		
		return sameFlag ? candidateMethods.get(0) : null;
	}
	
	private static boolean isResolvedTypeMatch(Method genericMethod, Method candidateMethod, Map<TypeVariable<?>, Type> typeVariableMap) {
		Type[] genericParameters = genericMethod.getGenericParameterTypes();
		Class<?>[] candidateParameters = candidateMethod.getParameterTypes();
		
		if (genericParameters.length != candidateParameters.length) {
			return false;
		}
		
		for (int i = 0; i < genericParameters.length; i++) {
			Type genericParameter = genericParameters[i];
			Class<?> candidateParameter = candidateParameters[i];
			
			if (candidateParameter.isArray()) {
				Type rawType = GenericTypeResolver.getRawType(genericParameter, typeVariableMap);
				
				// �Ƿ��� ������������
				if (rawType instanceof GenericArrayType) {
					if (!candidateParameter.getComponentType().equals(
							GenericTypeResolver.resolveType(((GenericArrayType) rawType).getGenericComponentType(), typeVariableMap))) {
						return false;
					}
					break;
				}
			}
			
			Class<?> resolvedParameter = GenericTypeResolver.resolveType(genericParameter, typeVariableMap);
			if (!candidateParameter.equals(resolvedParameter)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isBridgeCandidateFor(Method candidateMethod, Method bridgeMethod) {
		// ��ѡ���������Žӷ������Ҳ���Ŀ�귽���������ֺͲ�����������Ŀ�귽����ͬ
		return (!candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod) &&
				candidateMethod.getName().equals(bridgeMethod.getName()) &&
				candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length);
	}
}
