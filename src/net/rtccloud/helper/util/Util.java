package net.rtccloud.helper.util;

import net.rtccloud.helper.util.CustomReportSender.Mode;
import net.rtccloud.helper.util.CustomReportSender.ReportException;

import org.acra.ACRA;

import android.os.Build;


/**
 * Util helper class.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 * 
 */
public class Util {

	/**
	 * @param minify
	 *            <code>true</code> if the String must be without space, false otherwise.
	 * @return The name of the device.
	 */
	public static String getDeviceName(boolean minify) {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String res;
		if (model.startsWith(manufacturer)) {
			res = capitalize(model);
		} else {
			res = capitalize(manufacturer) + " " + model;
		}
		return minify ? res.replace(" ", "-") : res;
	}

	/**
	 * @param s
	 *            The string to capitalize.
	 * @return The provided String with uppercased words.
	 */
	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		}
		return Character.toUpperCase(first) + s.substring(1);
	}

	/**
	 * Call this method to generate a report and send it.
	 */
	public static void sendReport() {
		CustomReportSender.MODE = Mode.REPORT;
		ACRA.getErrorReporter().handleSilentException(new ReportException());
	}
}
