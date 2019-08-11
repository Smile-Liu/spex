package org.spex.util;

public class LoggerUtil {

	public static void info(String message) {
		System.out.println(message);
	}
	
	public static void error(String message) {
		System.err.println(message);
	}
	
	public static void error(String message, Throwable ex) {
		System.err.println(message + ":" + ex.getMessage());
		ex.printStackTrace();
	}
}
