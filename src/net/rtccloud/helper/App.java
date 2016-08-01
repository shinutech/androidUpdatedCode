package net.rtccloud.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import net.rtccloud.helper.util.CustomReportSender;
import net.rtccloud.helper.util.Scheme;
import net.rtccloud.helper.util.Scheme.Parameter;
import net.rtccloud.helper.util.Util;
import net.rtccloud.sdk.Logger;
import net.rtccloud.sdk.Logger.LoggerLevel;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine.UserType;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.github.anrwatchdog.ANRWatchDog;

//import net.rtccloud.helper.R;


// This is the ACRA configuration anotation.
// More intel at https://github.com/ACRA/acra
/**
 * Application object, mainly used for debugging purposes and to fetch custom data.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
@ReportsCrashes(formKey = "",
mailTo = "mobilesdk@weemo.com",
reportType = HttpSender.Type.JSON,
forceCloseDialogAfterToast = false,
mode = ReportingInteractionMode.DIALOG,
//resToastText = "R.string.crash_toast_text",
//resDialogIcon = "R.drawable.ic_launcher",
//resDialogTitle = "R.string.app_name",
//resDialogText = "R.string.crash_dialog_text",
logcatArguments = { "-t", "10000", "-v", "time" },
		//resDialogOkToast = R.string.crash_dialog_ok_toast,
//resDialogOkToast = "R.string.crash_dialog_ok_toast",
		customReportContent = {
			ReportField.REPORT_ID,
			ReportField.BRAND,
			ReportField.PRODUCT,
			ReportField.PHONE_MODEL,
			ReportField.ANDROID_VERSION,
			ReportField.APP_VERSION_NAME,
			ReportField.APP_VERSION_CODE,
			ReportField.BUILD,
			ReportField.STACK_TRACE,
			ReportField.INITIAL_CONFIGURATION,
			ReportField.CRASH_CONFIGURATION,
			ReportField.DISPLAY,
			ReportField.USER_APP_START_DATE,
			ReportField.USER_CRASH_DATE,
			ReportField.LOGCAT,
			ReportField.EVENTSLOG,
			ReportField.RADIOLOG,
			ReportField.INSTALLATION_ID,
			ReportField.DEVICE_FEATURES,
			ReportField.ENVIRONMENT,
			ReportField.SETTINGS_SYSTEM,
			ReportField.THREAD_DETAILS })
@SuppressWarnings("boxing")
public class App extends Application {

	/** Scheme used to store url scheme data */
	private static Scheme scheme;

	/** Default value for appID */
	public static String defaultAppID;
	/** Default value for authUrl */
	public static String defaultAuthUrl;
	/** Default value for willUseAuth */
	public static boolean defaultWillUseAuth;
	/** Default value for userID */
	public static String defaultUserID = Util.getDeviceName(true);
	/** Default value for displayName */
	public static String defaultDisplayName = Util.getDeviceName(false);
	/** Default value for calleeId */
	public static String defaultCalleeId;
	/** Default value for userType */
	public static UserType defaultUserType = UserType.INTERNAL;

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.setGlobalLevel(LoggerLevel.VERBOSE);

		/* Initialize ACRA to report errors */
		ACRA.init(this);
		ACRA.getErrorReporter().setReportSender(new CustomReportSender(this));
		
		breadcrumb("Rtcc SDK version %s", Rtcc.getVersionFull(this));

		/*
		 * Starts the ANR WatchDog. More intel at https://github.com/SalomonBrys/ANR-WatchDog
		 */
		new ANRWatchDog().start();

		fetchMetaData();
	}

	/**
	 * @return The Scheme singleton
	 */
	public static Scheme getScheme() {
		if (scheme == null) {
			scheme = new Scheme();
		}
		return scheme;
	}

	/**
	 * Initialize the scheme and replace the default values
	 * 
	 * @param intent
	 */
	public static void initScheme(Intent intent) {
		getScheme().init(intent);
		App.defaultAppID = scheme.get(Parameter.APP_ID, App.defaultAppID);
		App.defaultAuthUrl = scheme.get(Parameter.AUTH_URL, App.defaultAuthUrl);
		App.defaultWillUseAuth = "1".equals(scheme.get(Parameter.WILL_USE_AUTH, App.defaultWillUseAuth ? "1" : "0")) ? true : false;
		App.defaultUserID = scheme.get(Parameter.USER_ID, App.defaultUserID);
		App.defaultDisplayName = scheme.get(Parameter.DISPLAY_NAME, App.defaultDisplayName);
		App.defaultUserType = "1".equals(scheme.get(Parameter.USER_TYPE, App.defaultUserType == UserType.EXTERNAL ? "1" : "0")) ? UserType.EXTERNAL : UserType.INTERNAL;
		App.defaultCalleeId = scheme.get(Parameter.CALLEE_ID, App.defaultCalleeId);
		
		breadcrumb("initScheme()  defaultAppID:%s defaultAuthUrl:%s defaultWillUseAuth:%s defaultUserID:%s defaultDisplayName:%s defaultUserType:%s defaultCalleeId:%s", defaultAppID, defaultAuthUrl, defaultWillUseAuth, defaultUserID, defaultDisplayName, defaultUserType, defaultCalleeId);
	}

	/**
	 * Fetch the AppID value from the AndroidManifest.xml file.
	 */
	private void fetchMetaData() {
		try {
			Bundle bundle = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData;

			String valueAppId = bundle.getString(Parameter.APP_ID.key, null);
			String valueAuthUrl = bundle.getString(Parameter.AUTH_URL.key, null);
			int valueWillUseAuth = bundle.getInt(Parameter.WILL_USE_AUTH.key, -1);

			defaultAppID = valueAppId;
			defaultAuthUrl = valueAuthUrl;
			defaultWillUseAuth = "1".equals(String.valueOf(valueWillUseAuth));

			if (TextUtils.isEmpty(defaultAppID)) {
				Log.e(App.class.getSimpleName(), "appid is empty! The corresponding meta-data needs to be set in AndroidManifest.xml");
			}
			if (valueWillUseAuth != 0 && valueWillUseAuth != 1) {
				Log.e(App.class.getSimpleName(), "willuseAuth is incorrect! The corresponding meta-data needs to be set in AndroidManifest.xml");
			}
			if (defaultWillUseAuth && TextUtils.isEmpty(valueAuthUrl)) {
				Log.e(App.class.getSimpleName(), "authurl is empty! The corresponding meta-data needs to be set in AndroidManifest.xml");
			}

		} catch (Exception e) {
			Log.e(App.class.getSimpleName(), "Error trying to retrieve meta-data from AndroidManifest.xml");
		}
		breadcrumb("fetchMetaData()  defaultAppID:%s defaultAuthUrl:%s defaultWillUseAuth:%s", defaultAppID, defaultAuthUrl, defaultWillUseAuth);
	}

	/**
	 * Detects overriding instructions. Will restart the app if returns true;
	 * 
	 * @param intent
	 *            The {@link Intent} to crawl.
	 * @param context
	 *            The {@link Context} to use.
	 * @return <code>true</code> if it contains overriding instructions, <code>false</code> otherwise.
	 */
	public static boolean containsOverridingInstructions(Intent intent, Context context) {
		Uri data = intent.getData();
		if (data != null) {
			try {
				String str = data.getScheme();
				if (!TextUtils.isEmpty(str) && str.contains("rtccsdk")) {
					Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
					i.setData(data);
					PendingIntent mPendingIntent = PendingIntent.getActivity(context, 1111, i, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
					android.os.Process.killProcess(android.os.Process.myPid());
					return true;
				}
			} catch (UnsupportedOperationException ignore) {
				// NO-OP
			}
		}
		return false;
	}
	
	/** Date format for breadcrumbs */
	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS -- ");
	/** Trail of breadcrumbs */
	private static LinkedList<String> BREADCRUMBS = new LinkedList<String>();

	/**
	 * Drop a breadcrumb
	 * 
	 * @param format
	 * @param args
	 */
	public static void breadcrumb(String format, Object... args) {
		BREADCRUMBS.add(SIMPLE_DATE_FORMAT.format(new Date()) + String.format(format, args));
	}

	/**
	 * @return The trail of breadcrumbs
	 */
	public static String getBreadcrumbs() {
		StringBuilder sb = new StringBuilder();
		for (String str : BREADCRUMBS) {
			sb.append(str).append("\n");
		}
		return sb.toString();
	}
}
