package org.spex.web.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.spex.beans.BeanWrapper;
import org.spex.beans.PropertyAccessorFactory;
import org.spex.beans.PropertyValue;
import org.spex.beans.PropertyValues;
import org.spex.beans.factory.MutablePropertyValues;
import org.spex.util.LoggerUtil;
import org.spex.util.StringUtils;

public abstract class HttpServletBean extends HttpServlet {

	private static final long serialVersionUID = -8482577445973223933L;

	private final Set<String> requiredProperties = new HashSet<String>();
	
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}
	
	@Override
	public final void init() throws ServletException {
		LoggerUtil.info("��ʼ��ʼ��Servlet��" + getServletName());
		
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
		
		// BeanWrapper���˴�װ��HttpServletBean
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
		
		initBeanWrapper(bw);
		
		bw.setPropertyValues(pvs);
		
		initServletBean();
		
		LoggerUtil.info("Servlet '" + getServletName() + "'�������");
	}
	
	@Override
	public final String getServletName() {
		return getServletConfig() != null ? getServletConfig().getServletName() : null;
	}
	
	/**
	 * ��ʼ��װ����HttpServletBean��BeanWrapper
	 * ��������ȥʵ��
	 * @param bw
	 */
	protected void initBeanWrapper(BeanWrapper bw) {}
	
	/**
	 * �÷���ִ��ǰ���������Զ�Ҫ�������
	 * ��HttpServletBean���ǿ�ʵ��
	 * @throws ServletException
	 */
	protected void initServletBean() throws ServletException {}
	
	private static class ServletConfigPropertyValues extends MutablePropertyValues {
		
		public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties) throws ServletException {
			
			Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet<String>(requiredProperties) : null;
					
			Enumeration<?> en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String property = (String) en.nextElement();
				Object value = config.getInitParameter(property);
				
				addPropertyValue(new PropertyValue(property, value));
				
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}
			
			if (missingProps != null && missingProps.size() > 0) {
				throw new ServletException("��ʼ��Servlet '" + config.getServletName() + "'ʧ�ܣ�" +
						"��Ϊû���ҵ�����ָ����������ԣ�" + StringUtils.collectionToCommaDelimitedString(missingProps));
			}
		}
	}
}
