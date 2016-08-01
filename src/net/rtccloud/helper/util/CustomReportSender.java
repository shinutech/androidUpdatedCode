package net.rtccloud.helper.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import com.synsormed.mobile.R;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Rtcc;

/**
 * This class is provided to ACRA to generate a report.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class CustomReportSender implements ReportSender {

	/** The application context */
	private final Application ctx;

	/**
	 * Define the Report type: either {@link #CRASH} or {@link #REPORT}.
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public enum Mode {
		/** CRASH */
		CRASH("Crash"),
		/** REPORT */
		REPORT("Report");

		/** The string value of the {@link Mode} */
		public final String value;

		/**
		 * Default Constructor with a provided value.
		 * 
		 * @param value
		 */
		private Mode(String value) {
			this.value = value;
		}
	}

	/** This is used to distinguish a Crash from a simple Report */
	public static Mode MODE = Mode.CRASH;

	/**
	 * Exception without stack trace, used for manual error reporting.
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public static class ReportException extends Exception {

		/** Serial */
		private static final long serialVersionUID = 1L;

		/** Constructor */
		public ReportException() {
			super("NO EXCEPTION, this is a report");
		}

		@Override
		public Throwable fillInStackTrace() {
			return this;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            <b>Need to be the {@link Application}'s {@link Context}</b>
	 */
	public CustomReportSender(final Application ctx) {
		this.ctx = ctx;
	}

	@Override
	public void send(final CrashReportData errorContent) throws ReportSenderException {
		final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { ACRA.getConfig().mailTo() });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, buildSubject());
		emailIntent.putExtra(Intent.EXTRA_TEXT, this.ctx.getString(R.string.crash_email_body));
		emailIntent.putExtra(Intent.EXTRA_STREAM, saveFile(errorContent));

		if (emailIntent.resolveActivity(this.ctx.getPackageManager()) != null) {
			this.ctx.startActivity(emailIntent);
		} else {
			Log.e(CustomReportSender.class.getSimpleName(), "Error while sending the report. No app found on the device to handle email.");
		}
		MODE = Mode.CRASH;
	}

	/**
	 * Save the file with the provided error content which will be send by email
	 * 
	 * @param errorContent
	 *            The content
	 * @return The uri of the created file
	 * @throws ReportSenderException
	 *             if the the isn't successfully saved
	 */
	@SuppressLint("DefaultLocale")
	private Uri saveFile(final CrashReportData errorContent) throws ReportSenderException {
		try {
			final File outputFile = new File(this.ctx.getExternalCacheDir(), MODE.value.toLowerCase() + "_" + errorContent.get(ReportField.REPORT_ID) + ".log");
			final FileOutputStream outputStream = new FileOutputStream(outputFile);
			try {
				outputStream.write(buildBody(errorContent).getBytes("UTF-8"));
				outputStream.flush();
			} finally {
				outputStream.close();
			}
			return Uri.fromFile(outputFile);
		} catch (IOException e) {
			throw new ReportSenderException(e.getMessage(), e);
		}
	}

	/**
	 * @return The subject of the email
	 */
	private String buildSubject() {
		if (MODE == null) {
			MODE = Mode.CRASH;
		}
		return this.ctx.getString(R.string.crash_email_subject, this.ctx.getString(R.string.app_name), Rtcc.getVersionFull(this.ctx), MODE.value, Util.getDeviceName(false));
	}

	/**
	 * @param errorContent
	 *            The crash report data
	 * @return The body of the email containing the crash report
	 */
	private static String buildBody(final CrashReportData errorContent) {
		ReportField[] fields = ACRA.getConfig().customReportContent();
		if (fields.length == 0) {
			fields = ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS;
		}

		final StringBuilder builder = new StringBuilder();

		for (final ReportField field : fields) {
			builder.append(field.toString()).append(":\n").append(errorContent.get(field)).append("\n\n");
		}

		builder.append("BREADCRUMBS:\n").append(App.getBreadcrumbs());
		return builder.toString();
	}

}
