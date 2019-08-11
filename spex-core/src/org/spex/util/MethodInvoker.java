package org.spex.util;

public class MethodInvoker {

	
	/**
	 * ����������ͺͲ�����ƥ���
	 * ��ÿ��ֵ��ƥ��ȼ���������
	 * @param paramsType
	 * @param args
	 * @return
	 */
	public static int getTypeDifferenceWeight(Class<?>[] paramTypes, Object[] args) {
		int result = 0;
		for (int i = 0; i < paramTypes.length; i++) {
			if (!ClassUtils.isAssignableValue(paramTypes[i], args[i])) {
				return Integer.MAX_VALUE;
			}
			
			if (args[i] != null) {
				Class<?> paramType = paramTypes[i];
				Class<?> supperClass = args[i].getClass().getSuperclass();
				
				while (supperClass != null) {
					if (supperClass.equals(paramType)) {
						result += 2;
						supperClass = null;
					} else if (ClassUtils.isAssignableFrom(paramType, supperClass)) {
						result += 2;
						supperClass = supperClass.getSuperclass();
					} else {
						supperClass = null;
					}
				}
				
				if (paramType.isInterface()) {
					result += 1;
				}
			}
		}
		return result;
	}
}
