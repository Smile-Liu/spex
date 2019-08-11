package org.spex.web.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MultipartFile {

	/**
	 * @return Form表单中的参数名称
	 */
	String getName();
	
	/**
	 * @return 在客户系统中文件的名称（可能包含路径信息）
	 */
	String getOriginalFileName();
	
	/**
	 * @return 文件的Content Type
	 */
	String getContentType();
	
	/**
	 * @return 上传文件是否是空的、没有内容、没有选中文件
	 */
	boolean isEmpty();
	
	/**
	 * @return 文件大小
	 */
	long getSize();
	
	/**
	 * @return 文件内容的字节数组
	 * @throws IOException
	 */
	byte[] getBytes() throws IOException;
	
	InputStream getInputStream() throws IOException;
	
	/**
	 * 将上传的文件转换成目标系统的文件
	 * @param dest 目标系统文件
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;
	
}
