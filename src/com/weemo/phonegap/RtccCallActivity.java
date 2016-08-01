package com.weemo.phonegap;

import javax.annotation.Nullable;

import java.util.ArrayList;

//import com.weemo.listener.*;
import net.rtccloud.helper.controller.StatusBarController;
import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.listener.*;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

import com.synsormed.mobile.R;


//import net.rtccloud.helper.fragment.ConferencePanelFragment;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;


/**
 * This is the activity in which calls will take place for phone devices. In the
 * manifest, we have declared this activity landscape blocked. This is because
 * we don't want to handle system orientation (like in tablets). In Android
 * phones, the cameras always works best in landscape mode (this is their
 * default mode).
 * 
 * We will handle ourselves the rotation of the ui buttons to match the device
 * rotation (in the fragment).
 */
public class RtccCallActivity extends Activity implements DialogInterface.OnCancelListener, OnCallFragmentListener, OnFullScreenListener {

	/** Mimic the auto-generated file R.java */
	static class R {
		/** integer resources */
		static class integer {
			/** R.integer.camera_correction */
			static int camera_correction;
		}

		/**
		 * Initialise the {@link R} class
		 * 
		 * @param r
		 *            The {@link Resources} object
		 * @param pn
		 *            The packageName as String
		 */
		static void init(Resources r, String pn) {
			R.integer.camera_correction = r.getIdentifier("camera_correction", "integer", pn);
		}
	}

	/** The current call */
	protected @Nullable
	Call call;

	/** Tag for call identifier (extra data) */
	private static final String EXTRA_CALLID = "callId";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		R.init(getResources(), getPackageName());

		// The callId must be provided in the intent that started this activity
		// If it is not, we finish the activity
		final int callId = getIntent().getIntExtra(EXTRA_CALLID, -1);
		if (callId == -1) {
			finish();
			return;
		}

		// Weemo must be initialized before starting this activity
		// If it is not, we finish the activity
		final RtccEngine rtcc = Rtcc.instance();
		if (rtcc == null) {
			finish();
			return;
		}

		// The call with the given ID must exist before starting this activity
		// If it is not, we finish the activity
		this.call = rtcc.getCall(callId);
		if (this.call == null) {
			finish();
			return;
		}

		if (savedInstanceState == null) {
			((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(true);
		}

		//setTitle(this.call.getContactDisplayName());

		setTitle("TestTitleAmin");
		
		// Add the call window fragment
		if (savedInstanceState == null) {
			Log.d("AminLog","I am about to call the window fragment");
			//RtccCallFragment myFrag = RtccCallFragment.newInstanceCall(this.call);
			getFragmentManager()
					.beginTransaction()
					.replace(android.R.id.content,
                            CallFragment.newInstanceCall(this.call))
                                    //RtccCallFragment.newInstanceCall(this.call))
                                    //RtccCallFragment.newInstance(callId, com.weemo.phonegap.RtccCallFragment.TouchType.SLIDE_CONTROLS_FULLSCREEN, getResources().getInteger(R.integer.camera_correction), false))
                                    .commit();
		}

		// Register as event listener
		Rtcc.eventBus().register(this);
	}

	@Override
	public void onDestroy() {
		// Unregister as event listener
		Rtcc.eventBus().unregister(this);

		// When we leave this activity, we stop the video.
		if (this.call != null) {
			this.call.removeVideoOut();
			//ArrayList<Contact> mycontacts = this.call.getContacts();
			//int contactId = mycontacts.get(0).getId();
			//this.call.removeVideoIn(contactId);
			this.call.removeVideoIn(0);
		}

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// We allow leaving this activity only if specified
		if (getIntent().getBooleanExtra("canComeBack", false)) {
			super.onBackPressed();
		}
	}

	/**
	 * This listener catches CallStatusChangedEvent 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent 3. It's
	 * activity object has been registered with
	 * Weemo.getEventBus().register(this) in onCreate()
	 * 
	 * @param event
	 *            The event
	 */
	@RtccEventListener
	public void onCallStatusChanged(final StatusEvent event) {
		// First, we check that this event concerns the call we are monitoring
		if (event.getCall().getCallId() != this.call.getCallId()) {
			return;
		}

		// If the call has ended, we finish the activity (as this activity is
		// only for an active call)
		if (event.getStatus() == CallStatus.ENDED) {
			finish();
		}
	}

	/**
	 * This listener catches CallStatusChangedEvent 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent 3. It's
	 * activity object has been registered with
	 * Weemo.getEventBus().register(this) in onCreate()
	 * 
	 * @param event
	 *            The event
	 
	@RtccEventListener
	public void onCanCreateCallChanged(final CanCreateCallChangedEvent event) {
		final Error error = event.getError();
		if (error == CanCreateCallChangedEvent.Error.CLOSED) {
			Toast.makeText(this, error.description(), Toast.LENGTH_SHORT).show();
			finish();
		}
	}
*/
	@Override
	public void onCancel(DialogInterface dialog) {
		this.call.hangup();
	}
	
	@Override
	public void onHangup(Call call) {

		getFragmentManager().popBackStack();
		//onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ended, call == null ? "" : DateUtils.formatElapsedTime(call.getCallDuration() / 1000L)), false);
		//getActionBar().show();
	}
	
	@Override
	public void onStatusUpdate(String title, String subtitle, boolean showProgress) {
		//none
	}
	
	public void enableDrawerToggle(boolean enable){
		
	}
	
	public void onFullScreen(boolean enable){
		
	}

    public void onShowStatusBar(String title, StatusBarController.StatusBarAction action, Bundle bundle){

    }

}
