package net.rtccloud.helper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.synsormed.mobile.R;

import net.rtccloud.helper.controller.StatusBarController;
import net.rtccloud.helper.controller.StatusBarController.StatusBarAction;
import net.rtccloud.helper.controller.StatusBarController.StatusBarListener;
import net.rtccloud.helper.fragment.AuthenticatedFragment;
import net.rtccloud.helper.fragment.BroadcastMeetingPointDialogFragment;
import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.fragment.ConferencePanelFragment;
import net.rtccloud.helper.fragment.LoginFragment;
import net.rtccloud.helper.fragment.RingingDialogFragment;
import net.rtccloud.helper.listener.OnAuthenticatedFragmentListener;
import net.rtccloud.helper.listener.OnCallFragmentListener;
import net.rtccloud.helper.listener.OnFullScreenListener;
import net.rtccloud.helper.listener.OnLoginFragmentListener;
import net.rtccloud.helper.service.RtccService;
import net.rtccloud.helper.service.RtccService.Action;
import net.rtccloud.helper.util.BackgroundTimer;
import net.rtccloud.helper.util.Scheme.Parameter;
import net.rtccloud.helper.util.Util;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.StartingOptions;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine.Status;

import java.text.DateFormat;
import java.util.Date;
/**
 * This is the main {@link Activity} of the Helper. It is responsible for displaying status messages in the {@link ActionBar} via {@link #onStatusUpdate(String, String, boolean)}, the
 * {@link RtccService} to detect calls, as well as managing the main {@link Fragment}s :
 * <ul>
 * <li>{@link LoginFragment}</li>
 * <li>{@link AuthenticatedFragment}</li>
 * <li>{@link RingingDialogFragment}</li>
 * <li>{@link CallFragment}</li>
 * <li>{@link ConferencePanelFragment}</li>
 * </ul>
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class MainActivity extends Activity implements OnLoginFragmentListener, OnAuthenticatedFragmentListener, OnCallFragmentListener, OnFullScreenListener, StatusBarListener {

	/** Key to persist the ActionBar title */
	private static final String KEY_TITLE = "KEY_TITLE";
	/** Key to persist the ActionBar subtitle */
	private static final String KEY_SUBTITLE = "KEY_SUBTITLE";
	/** Key to persist the progress state */
	private static final String KEY_PROGRESS = "KEY_PROGRESS";

	/** Controller of the status bar */
	private StatusBarController mStatusBarController;
	/** Flag to detect progress */
	private boolean mIsInProgress = false;

	/** Drawer layout to display the {@link ConferencePanelFragment} */
	private DrawerLayout mDrawerLayout;
	/** Drawer toggle button */
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initDrawer();
		enableDrawerToggle(false);

		this.mStatusBarController = new StatusBarController(findViewById(R.id.statusbar), this);
		onStatusUpdate(getString(R.string.app_name), null, false);

		/* If this is the first time, display the right Fragment on the screen */
		if (savedInstanceState == null) {
			App.initScheme(getIntent());
			if (Rtcc.getEngineStatus() == Status.AUTHENTICATED) {
				getFragmentManager().beginTransaction().add(R.id.form_container, AuthenticatedFragment.newInstance(), AuthenticatedFragment.TAG).commit();
			} else {
				getFragmentManager().beginTransaction().add(R.id.form_container, LoginFragment.newInstance(), LoginFragment.TAG).commit();
			}
			startService(new Intent(this, RtccService.class));
		}
	}

	/**
	 * Initialize the {@link DrawerLayout}
	 */
	private void initDrawer() {
		this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		this.mDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawerLayout, R.drawable.ic_navigation_drawer, 0, 0) {

			/* Called when a drawer has settled in a completely closed state. */
			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			/* Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		this.mDrawerLayout.setDrawerListener(this.mDrawerToggle);

	}

	/**
	 * Detect if there is any action (maybe associated to a current {@link Call}) provided by the {@link Bundle} inside the {@link Intent}.
	 * 
	 * @param intent
	 *            Can contains details about the action.
	 */
	private void detectAction(Intent intent) {
		Bundle bundle = intent == null ? null : intent.getExtras();
		if (intent == null || bundle == null) {
			return;
		}
		/* Fetch the provided Call and Action */
		Action action = Action.fromOrdinal(bundle.getInt(RtccService.EXTRA_ACTION));
		Call call = Rtcc.instance() == null ? null : Rtcc.instance().getCall(bundle.getInt(RtccService.EXTRA_CALLID, -1));
		if ((action == Action.RINGING || action == Action.ACCEPT_CALL_VIDEO || action == Action.ACCEPT_CALL_AUDIO || action == Action.REJECT_CALL || action == Action.HANGUP_CALL || action == Action.ONGOING_CALL || action == Action.RESUME_CALL) && call == null) {
			return;
		}
		switch (action) {
		case RINGING:
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ringing), true);
			RingingDialogFragment.show(call, getFragmentManager());
			break;
		case ACCEPT_CALL_VIDEO:
			RingingDialogFragment.hide(getFragmentManager());
			App.breadcrumb("Call.resume()");
			if (call != null) {
				call.resume();
			}
			break;
		case ACCEPT_CALL_AUDIO:
			RingingDialogFragment.hide(getFragmentManager());
			StartingOptions options = new StartingOptions.Builder().setVideoEnabled(false).build();
			App.breadcrumb("Call.resume(options=%s)", options);
			if (call != null) {
				call.resume(options);
			}
			break;
		case REJECT_CALL:
			RingingDialogFragment.hide(getFragmentManager());
			App.breadcrumb("Call.hangup()");
			if (call != null) {
				call.hangup();
			}
			break;
		case HANGUP_CALL:
			App.breadcrumb("Call.hangup()");
			if (call != null) {
				call.hangup();
			}
			break;
		case MESSAGE:
			String message = new String(bundle.getByteArray(RtccService.EXTRA_PAYLOAD));
			if (!TextUtils.isEmpty(message) && message.startsWith(BroadcastMeetingPointDialogFragment.MEETINGPOINT_START_TAG) && message.endsWith(BroadcastMeetingPointDialogFragment.MEETINGPOINT_END_TAG)) {
				String str = "<b>" + bundle.getString(RtccService.EXTRA_DISPLAY_NAME) + "</b> is broadcasting a Meeting Point";
				bundle.putString(RtccService.EXTRA_MEETING_POINT_ID, message.replace(BroadcastMeetingPointDialogFragment.MEETINGPOINT_START_TAG, "").replace(BroadcastMeetingPointDialogFragment.MEETINGPOINT_END_TAG, ""));
				onShowStatusBar(str, StatusBarAction.MEETING_POINT, bundle);
			} else {
				String sender = "<i>Message from <b>" + bundle.getString(RtccService.EXTRA_DISPLAY_NAME) + "</b>:</i><br/><br/>";
				onShowStatusBar(TextUtils.concat(sender, message).toString(), StatusBarAction.REPLY, bundle);
			}
			break;
		case ONGOING_CALL:
		case RESUME_CALL:
		case UNKNOWN:
		default:
			/* No-op, only resume the activity */
			break;
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void enableDrawerToggle(boolean enable) {
		if (this.mDrawerLayout != null && this.mDrawerToggle != null) {
			this.mDrawerLayout.setDrawerLockMode(enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			this.mDrawerToggle.setDrawerIndicatorEnabled(enable);
		}

		getActionBar().setDisplayHomeAsUpEnabled(enable);
		getActionBar().setHomeButtonEnabled(enable);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		/* Sync the toggle state after onRestoreInstanceState has occurred */
		this.mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (App.containsOverridingInstructions(intent, this)) {
			return;
		}
		detectAction(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		/* Here we can cancel the background countdown if there is any, and return to foreground */
		BackgroundTimer.cancelCountDown();
		if (Rtcc.instance() != null && Rtcc.instance().isInBackground()) {
			App.breadcrumb("RtccEngine.goToForeground()");
			Rtcc.instance().goToForeground();
		}

		/* Start the CallFragment if there is a current Call and no CallFragment */
		if (Rtcc.instance() != null && Rtcc.instance().getCurrentCall() != null) {
			Fragment callFragment = getFragmentManager().findFragmentByTag(CallFragment.TAG);
			if (callFragment == null) {
				CallFragment.show(getFragmentManager());
			}
			Fragment conferencePanelFragment = getFragmentManager().findFragmentByTag(ConferencePanelFragment.TAG);
			if (conferencePanelFragment == null) {
				ConferencePanelFragment.show(getFragmentManager());
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		/* Here we need to go to background in order to save battery life. */
		if (!this.isChangingConfigurations()) {
			if (Rtcc.instance() != null && !Rtcc.instance().isInBackground()) {
				if (Rtcc.instance().getCurrentCall() == null) {
					BackgroundTimer.startCountDown();
				}
			}
		}
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		this.mStatusBarController.onSaveInstanceState(outState);
		outState.putString(KEY_TITLE, getActionBar().getTitle().toString());
		outState.putString(KEY_SUBTITLE, getActionBar().getSubtitle() == null ? null : getActionBar().getSubtitle().toString());
		outState.putBoolean(KEY_PROGRESS, this.mIsInProgress);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.mStatusBarController.onRestoreInstanceState(savedInstanceState);
		String title = savedInstanceState.getString(KEY_TITLE);
		String subtitle = savedInstanceState.getString(KEY_SUBTITLE);
		boolean progress = savedInstanceState.getBoolean(KEY_PROGRESS);
		onStatusUpdate(title, subtitle, progress);
	}

	@Override
	public void onBackPressed() {
		/* Disable back button if a Call is active */
		Fragment callFragment = getFragmentManager().findFragmentByTag(CallFragment.TAG);
		if (Rtcc.instance() != null && Rtcc.instance().getCurrentCall() != null && callFragment != null && callFragment.isVisible()) {
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_version_and_status);
		if (item != null) {
			item.setTitle(Html.fromHtml("<b>" + Rtcc.getVersionFull(this) + "</b> <i>" + Rtcc.getEngineStatus().name() + "</i>"));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (this.mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_report:
			Util.sendReport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStatusUpdate(String title, String subtitle, boolean showProgress) {
		ActionBar actionBar = getActionBar();
		//actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		//actionBar.setCustomView(R.layout.actionbar_title);
		actionBar.setTitle(title);
		actionBar.setSubtitle(TextUtils.isEmpty(subtitle) ? null : Html.fromHtml(subtitle));
		setProgressBarIndeterminateVisibility(showProgress);
		this.mIsInProgress = showProgress;
	}

	@Override
	public void onAuthenticated() {
		/* Show the {@link AuthenticatedFragment} */
		AuthenticatedFragment.show(getFragmentManager());
	}

	@Override
	public void onLogout() {
		/* Show the {@link LoginFragment} */
		LoginFragment.show(getFragmentManager());
		onStatusUpdate(getString(R.string.app_name), null, false);
		if (Rtcc.getEngineStatus() != Status.UNDEFINED) {
			App.breadcrumb("RtccEngine.disconnect()");
			Rtcc.instance().disconnect();
		}
	}

	@Override
	public void onCallActive(Call call) {
		/* Show the {@link CallFragment} */
		CallFragment.show(getFragmentManager());
		if (call != null && call.isConference()) {
			ConferencePanelFragment.show(getFragmentManager());
		}
	}

	@Override
	public void onHangup(Call call) {
		if (call != null && call.isConference()) {
			ConferencePanelFragment.hide(getFragmentManager());
		}
		getFragmentManager().popBackStack();
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ended, call == null ? "" : DateUtils.formatElapsedTime(call.getCallDuration() / 1000L)), false);
		getActionBar().show();
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onFullScreen(boolean enable) {
		if (enable) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			}
			getActionBar().hide();
		} else {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
	}

	@Override
	public void onShowStatusBar(String title, StatusBarAction action, Bundle bundle) {
		if (this.mStatusBarController != null) {
			this.mStatusBarController.showStatusBar(title, action, bundle);
		}
	}

	@SuppressWarnings("boxing")
	@Override
	public void onStatusBarClick(Bundle data, StatusBarAction action) {
		if (data == null) {
			return;
		}
		AuthenticatedFragment authenticatedFragment = (AuthenticatedFragment) getFragmentManager().findFragmentByTag(AuthenticatedFragment.TAG);
		String calleeID = data.getString(Parameter.CALLEE_ID.key);
		switch (action) {
		case CALL:
			authenticatedFragment.call(calleeID, true);
			break;
		case RETRY:
			authenticatedFragment.check(calleeID);
			break;
		case REPLY:
			if (Rtcc.instance() != null && data.containsKey(RtccService.EXTRA_SENDER_ID)) {
				final int id = data.getInt(RtccService.EXTRA_SENDER_ID);
				App.breadcrumb("RtccEngine.replyDataToContact(id=%d)", id);
				Rtcc.instance().replyDataToContact(("This is an auto-reply message!<br/><br/>" + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date())).getBytes(), id);
			}
			break;
		case MEETING_POINT:
			authenticatedFragment.acquireMeetingPoint(data.getString(RtccService.EXTRA_MEETING_POINT_ID));
			break;
		default:
			break;
		}
	}
}
