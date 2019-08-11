package org.spex.web.multipart;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface MultipartRequest {

	/**
	 * @return Form���е��ļ���������
	 */
	Iterator<String> getFileNames();
	
	/**
	 * @param name ������
	 * @return ��Ӧ���ļ�
	 */
	MultipartFile getFile(String name);
	
	/**
	 * @param name ������
	 * @return �ļ��б���ѡ��
	 */
	List<MultipartFile> getFiles(String name);
	
	Map<String, MultipartFile> getFileMap();
}
