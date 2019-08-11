package org.spex.web.multipart;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface MultipartRequest {

	/**
	 * @return Form表单中的文件参数名称
	 */
	Iterator<String> getFileNames();
	
	/**
	 * @param name 参数名
	 * @return 对应的文件
	 */
	MultipartFile getFile(String name);
	
	/**
	 * @param name 参数名
	 * @return 文件列表（多选）
	 */
	List<MultipartFile> getFiles(String name);
	
	Map<String, MultipartFile> getFileMap();
}
