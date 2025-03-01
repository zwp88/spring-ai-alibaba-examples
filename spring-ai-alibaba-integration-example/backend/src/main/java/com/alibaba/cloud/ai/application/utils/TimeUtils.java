package com.alibaba.cloud.ai.application.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public final class TimeUtils {

	private final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private TimeUtils() {
	}

	public static String getCurrentTime() {

		long currentTimeMillis = System.currentTimeMillis();

		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

		return dateTime.format(formatter);
	}

}
