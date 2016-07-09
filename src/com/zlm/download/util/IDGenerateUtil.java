package com.zlm.download.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author zhangliangming id自动生成器
 */
public class IDGenerateUtil {
	private static final AtomicInteger integer = new AtomicInteger(0);

	/**
	 * 生成id
	 * 
	 * @return
	 */
	public static String getId() {
		long time = System.currentTimeMillis();
		StringBuilder str = new StringBuilder(20);
		str.append(time);
		int intValue = integer.getAndIncrement();
		if (integer.get() >= 10000) {
			integer.set(0);
		}
		if (intValue < 10) {
			str.append("000");
		} else if (intValue < 100) {
			str.append("00");
		} else if (intValue < 1000) {
			str.append("0");
		}
		str.append(intValue);
		return str.toString();
	}

	/**
	 * 生成规定长度的id
	 * 
	 * @param length
	 * @return
	 */
	public static String getId(int length) {
		long time = System.currentTimeMillis();
		StringBuilder str = new StringBuilder(length);
		str.append(time);
		int intValue = integer.getAndIncrement();
		if (integer.get() >= 10000) {
			integer.set(0);
		}
		if (intValue < 10) {
			str.append("000");
		} else if (intValue < 100) {
			str.append("00");
		} else if (intValue < 1000) {
			str.append("0");
		}
		str.append(intValue);
		return str.toString();
	}

	/**
	 * 生成前缀为key的id
	 * 
	 * @param key
	 * @return
	 */
	public static String getId(String key) {
		long time = System.currentTimeMillis();
		StringBuilder str = new StringBuilder(18);
		str.append(time);
		int intValue = integer.getAndIncrement();
		if (integer.get() >= 10000) {
			integer.set(0);
		}
		if (intValue < 10) {
			str.append("000");
		} else if (intValue < 100) {
			str.append("00");
		} else if (intValue < 1000) {
			str.append("0");
		}
		str.append(intValue);
		return key + str.toString();
	}

	/**
	 * 生成前缀key和规定长度的id
	 * 
	 * @param key
	 * @param length
	 * @return
	 */
	public static String getId(String key, int length) {
		long time = System.currentTimeMillis();
		StringBuilder str = new StringBuilder(length);
		str.append(time);
		int intValue = integer.getAndIncrement();
		if (integer.get() >= 10000) {
			integer.set(0);
		}
		if (intValue < 10) {
			str.append("000");
		} else if (intValue < 100) {
			str.append("00");
		} else if (intValue < 1000) {
			str.append("0");
		}
		str.append(intValue);
		return key + str.toString();
	}
}