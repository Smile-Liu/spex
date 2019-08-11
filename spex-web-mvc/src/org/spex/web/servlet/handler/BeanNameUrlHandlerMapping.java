package org.spex.web.servlet.handler;

import java.util.ArrayList;
import java.util.List;

public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	@Override
	protected String[] determineUrlsForHandler(String beanName) {
		List<String> urls = new ArrayList<String>();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		return urls.toArray(new String[urls.size()]);
	}

}
