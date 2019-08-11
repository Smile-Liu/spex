package org.spex.web.servlet;

import javax.servlet.http.HttpServletRequest;

public interface HandlerMapping {
	
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";
	
	String PATH_WITHIN_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinMapping";
	

	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
}
