package com.intellivision.adas.demo.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.Logger;
import com.intellivision.adas.demo.logger.VCLog;

/**
 * This is a utility class mostly required for date conversions/formatting.
 * 
 */
public class DateTimeUtils {

	/**
	 * Method to get the formatted time
	 * 
	 * @param format
	 *            the format
	 * @return the formatted time.
	 */
	public static String getFormattedTime(String format) {
		String time = null;
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		time = simpleDateFormat.format(new Date(currentTimeMillis));
		return time;
	}

	/**
	 * Method to get the formatted time
	 * 
	 * @param format
	 *            the format
	 * @return the formatted time.
	 */
	public static String getFormattedTime() {
		String time = null;
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				Logger.TIMESTAMP_FORMAT);
		time = simpleDateFormat.format(new Date(currentTimeMillis));
		return time;
	}

	/**
	 * Method to get the formatted time
	 * 
	 * @param timestamp
	 *            the timestamp in milliseconds
	 * @param format
	 *            the format
	 * @return the formatted time.
	 */
	public static String getFormattedTime(long timestamp, String format) {
		String time = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		time = simpleDateFormat.format(new Date(timestamp));
		return time;
	}

	/**
	 * Method to get the no of dates between two specified dates.
	 * 
	 * @param strDate1
	 *            the startDate
	 * @param strDate2
	 *            the endDate
	 * @return the no of days between input dates
	 */
	public static int getNoOfDaysBetween(String strDate1, String strDate2) {
		long diff = 0;
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					Logger.FILE_NAME_FORMAT, Locale.US);

			Date date1 = format.parse(strDate1);
			Date date2 = format.parse(strDate2);

			diff = ((long) (date1.getTime() - date2.getTime()))
					/ (1000 * 60 * 60 * 24);
		} catch (Exception ex) {
			VCLog.error(
					Category.CAT_GENERAL,
					"DateTimeUtils: getNoOfDaysBetween: Exception->"
							+ ex.getMessage());
			ex.printStackTrace();
		}
		return (int) diff;
	}
}
