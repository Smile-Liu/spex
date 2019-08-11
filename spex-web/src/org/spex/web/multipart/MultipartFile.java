package org.spex.web.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MultipartFile {

	/**
	 * @return Form���еĲ�������
	 */
	String getName();
	
	/**
	 * @return �ڿͻ�ϵͳ���ļ������ƣ����ܰ���·����Ϣ��
	 */
	String getOriginalFileName();
	
	/**
	 * @return �ļ���Content Type
	 */
	String getContentType();
	
	/**
	 * @return �ϴ��ļ��Ƿ��ǿյġ�û�����ݡ�û��ѡ���ļ�
	 */
	boolean isEmpty();
	
	/**
	 * @return �ļ���С
	 */
	long getSize();
	
	/**
	 * @return �ļ����ݵ��ֽ�����
	 * @throws IOException
	 */
	byte[] getBytes() throws IOException;
	
	InputStream getInputStream() throws IOException;
	
	/**
	 * ���ϴ����ļ�ת����Ŀ��ϵͳ���ļ�
	 * @param dest Ŀ��ϵͳ�ļ�
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;
	
}
