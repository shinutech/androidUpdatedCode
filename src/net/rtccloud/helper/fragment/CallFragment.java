package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.helper.controller.CallViewController;
import net.rtccloud.helper.controller.FloatingWindowController;
import net.rtccloud.helper.controller.FloatingWindowTouchController;
import net.rtccloud.helper.controller.FullScreenController;
import net.rtccloud.helper.controller.ScreenShareController;
import net.rtccloud.helper.listener.OnCallControlClickListener;
import net.rtccloud.helper.listener.OnCallFragmentListener;
import net.rtccloud.helper.listener.OnFullScreenListener;
import net.rtccloud.helper.util.Ui;
import net.rtccloud.helper.view.CallControls;
import net.rtccloud.helper.view.CallControls.CallButton;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.AudioRoute;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Call.VideoProfile;
import net.rtccloud.sdk.Call.VideoSource;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.AudioRouteEvent;
import net.rtccloud.sdk.event.call.FloorListEvent;
import net.rtccloud.sdk.event.call.ParticipantEvent;
import net.rtccloud.sdk.event.call.ParticipantListEvent;
import net.rtccloud.sdk.event.call.ScreenShareInEvent;
import net.rtccloud.sdk.event.call.ScreenShareInSizeEvent;
import net.rtccloud.sdk.event.call.ScreenShareOutEvent;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.call.VideoInEvent;
import net.rtccloud.sdk.event.call.VideoInSizeEvent;
import net.rtccloud.sdk.event.call.VideoOutEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.view.ScreenShareInFrame;
import net.rtccloud.sdk.view.VideoInFrame;
import net.rtccloud.sdk.view.VideoOutPreviewFrame;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ActionBar.LayoutParams;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.util.Log;
import android.content.Intent;
import android.graphics.Matrix.ScaleToFit;

import com.synsormed.mobile.MainActivity;
//import com.synsormed.mobile.Synsormed;
import com.weemo.phonegap.CallContainer;
import com.weemo.phonegap.RtccAndroidPhonegap;

/**
 * This {@link Fragment} will be displayed when a {@link Call} gets {@link CallStatus#ACTIVE}.<br />
 * It can handle conference calls and regular 1:1 calls.<br/>
 * <br/>
 * The {@link CallFragment} uses mutliple controllers to organise and split behaviours and features into components, wich makes the code easy to read. These components are:
 * <ul>
 * <li>{@link CallViewController} handles views layout and size</li>
 * <li>{@link FloatingWindowController} handles floating window feature</li>
 * <li>{@link FloatingWindowTouchController} handles internal floating window behaviour</li>
 * <li>{@link FullScreenController} handles the fullscreen feature</li>
 * <li>{@link ScreenShareController} handles screen sharing feature</li>
 * <li>{@link CallControls} handles call controls through a button bar</li>
 * </ul>
 * To be notified of click events in the {@link CallControls} button bar, simply register a {@link OnCallControlClickListener}.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
@SuppressLint("NewApi") public class CallFragment extends Fragment implements OnCallControlClickListener {

	public CallContainer callContainer;
	/** Tag to identify the {@link CallFragment} */
	public static final String TAG = CallFragment.class.getSimpleName();

	/** The call associated with this Fragment */
	protected Call mCall;
    static Call mCall2;
	/** Callback used to dispatch an ended call */
	private OnCallFragmentListener mCallListener;

	/** Flag to detect if the Fragment was put into background during a {@link Call} */
	private boolean mHasGoneBackground = false;

	/** Controls the call views */
	protected CallViewController mCallViewController;
	/** Controls the floating window */
	protected FloatingWindowController mFloatingWindowController;
	/** Controls the screen share */
	protected ScreenShareController mScreenShareController;
	/** Controls the fullscreen */
	protected FullScreenController mFullScreenController;
	
    protected View callView;
	 
 

	/**
	 * Helper method to show a {@link CallFragment}.
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use.
	 */
	public static void show(FragmentManager fm) {
		fm.beginTransaction()/* .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out) */.add(R.id.call_container, CallFragment.newInstance(), CallFragment.TAG).addToBackStack(null).commit();
	}

	/**
	 * Factory method. Creates a new instance of this fragment.
	 * 
	 * @return A new instance of {@link CallFragment}.
	 */
	public static CallFragment newInstance() {
		return new CallFragment();
	}

    public static CallFragment newInstanceCall(Call currentCall){

        mCall2 = currentCall;
        return new CallFragment();
    }

	
	/**
	 * Required empty public constructor
	 */
	public CallFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		//this.mCall = Rtcc.instance().getCurrentCall();
        this.mCall = mCall2;
		if (this.mCallListener != null && (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED)) {
			this.mCallListener.onHangup(this.mCall);
			return;
		}

		if (this.mCallListener != null && this.mCall.isConference()) {
			this.mCallListener.enableDrawerToggle(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (this.mCallListener != null && this.mCall.isConference()) {
			this.mCallListener.enableDrawerToggle(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		this.mFullScreenController.onResume(this);
        if (this.mCallViewController == null) 
        {
            this.mCallViewController = new CallViewController(getActivity());
        }
		this.mCallViewController.update(false, true, true);
        
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED) {
			return null;
		}
		View view = inflater.inflate(this.mCall.isConference() ? R.layout.fragment_conference : R.layout.fragment_call, container, false);
        this.callView = view;
		VideoOutPreviewFrame selfView = (VideoOutPreviewFrame) view.findViewById(R.id.video_out);
		CallControls callControls = (CallControls) view.findViewById(R.id.call_control);
		callControls.setOnCallControlsListener(this);

		if (this.mFloatingWindowController == null) {
			this.mFloatingWindowController = new FloatingWindowController(getActivity());
		}
		if (this.mCallViewController == null) {
			this.mCallViewController = new CallViewController(getActivity());
		}
		if (this.mScreenShareController == null) {
			this.mScreenShareController = new ScreenShareController();
		}

		this.mFloatingWindowController.setActionBar(getActivity().getActionBar());
		this.mFloatingWindowController.init(view.findViewById(R.id.video_frame), view);

		this.mCallViewController.setCall(this.mCall);
		this.mCallViewController.setVideoViews(callControls, selfView, (VideoInFrame) view.findViewById(R.id.video_in_master), (VideoInFrame) view.findViewById(R.id.video_in_1), (VideoInFrame) view.findViewById(R.id.video_in_2), (VideoInFrame) view.findViewById(R.id.video_in_3),
				(VideoInFrame) view.findViewById(R.id.video_in_4));
		this.mCallViewController.setLabelViews((TextView) view.findViewById(R.id.label_in_master), (TextView) view.findViewById(R.id.label_in_1), (TextView) view.findViewById(R.id.label_in_2), (TextView) view.findViewById(R.id.label_in_3), (TextView) view.findViewById(R.id.label_in_4));
		this.mCallViewController.setFloatingWindowController(this.mFloatingWindowController);

		this.mScreenShareController.init(view.findViewById(R.id.share_container), savedInstanceState, this.mCall);

		this.mFullScreenController.setCall(this.mCall);
		this.mFullScreenController.setRoot(view);
		this.mFullScreenController.setCallControls(callControls);
		this.mFullScreenController.setFloatingWindowController(this.mFloatingWindowController);

		this.mFloatingWindowController.setFullScreenWindowController(this.mFullScreenController);

		this.mCall.setVideoOut(selfView);
		this.mCall.setScreenShareIn((ScreenShareInFrame) view.findViewById(R.id.screen_share_in));

		// Avoid ActionBar to be re-show after a rotation
		if (!this.mFloatingWindowController.isFloating()) {
			getActivity().getActionBar().hide();
		}

		view.post(new Runnable() {
			@Override
			public void run() {
				CallFragment.this.mCallViewController.update(true, true, true);
			}
		});
		ImageView encryptionImage=(ImageView)view.findViewById(R.id.encryptionIcon);
		int y=(int) callControls.getY();
		int height=callControls.getHeight();
		int screenWidth=view.getWidth();
		encryptionImage.setX(screenWidth-50);
		encryptionImage.setY(height+y+50);
		handleSwitchButtonRequired(view);
		return view;
	}
    
    private void hideUnusedButtons(View view)
    {
      
        CallControls callControls = (CallControls) view.findViewById(R.id.call_control);
        ImageButton fullButton=(ImageButton)callControls.findViewById(R.id.call_control_fullscreen);
        ImageButton scaleButton=(ImageButton)callControls.findViewById(R.id.call_control_scale_type);
        ImageButton shareButton=(ImageButton)callControls.findViewById(R.id.call_control_share);
        
        fullButton.setVisibility(View.GONE);
        scaleButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
    
    }
	
	private void handleSwitchButtonRequired(View view)
	{
		if(!RtccAndroidPhonegap.isProvider() || !RtccAndroidPhonegap.isDataAvailable())
		{
			CallControls callControls = (CallControls) view.findViewById(R.id.call_control);
			ImageButton switchButton=(ImageButton)callControls.findViewById(R.id.call_control_patient_info);
			switchButton.setVisibility(View.GONE);
		}
	}
	
	private ImageView paintEncryptionImage(View rootView)
	{	
		ImageView encryptionImage=new ImageView(getActivity());
		encryptionImage.setBackgroundResource(R.drawable.ic_call_controls_patient_info);
		encryptionImage.setScaleType(ScaleType.CENTER_INSIDE);
		LayoutParams imageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		encryptionImage.setLayoutParams(imageParams);
		CallControls callControls = (CallControls) rootView.findViewById(R.id.call_control);
		int y=(int) callControls.getY();
		int height=callControls.getHeight();
		int screenWidth=rootView.getWidth();
		encryptionImage.setX(screenWidth-50);
		encryptionImage.setY(height+y+10);
		return encryptionImage;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/* We instantiate the controller here instead of in onCreate() because onAttach() is called first */
		if (this.mFullScreenController == null) {
			this.mFullScreenController = new FullScreenController();
		}
		try {
			this.mCallListener = (OnCallFragmentListener) callContainer;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnCallFragmentListener");
		}
		try {
			this.mFullScreenController.onAttach((OnFullScreenListener) callContainer);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFullScreenListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mCallListener = null;
		this.mFullScreenController.onDetach();
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);

		Ui.setKeepScreenOn(getActivity().getWindow(), true);
		this.mFullScreenController.onStart();

		/* Force hangup if the call was ended outside of this fragment */
		if (this.mCallListener != null && (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED)) {
			this.mCallListener.onHangup(this.mCall);
			return;
		}

		if (this.mHasGoneBackground) {
			this.mHasGoneBackground = false;
		}
		/* restart the video */
		if (this.mCallViewController.isPaused()) {
			this.mCallViewController.resume();
			this.mCall.videoStart();
			this.mCallViewController.update(false, true, false);
		}
		/* restart the screen share */
		if (this.mScreenShareController.isPaused()) {
			this.mScreenShareController.postStart();
			this.mCallViewController.update(false, true, false);
		}
        
        //Just before Fragment is active, hide unused buttons on the callView
        if(this.callView != null){
            hideUnusedButtons(this.callView);
        }
	}

	@Override
	public void onStop() {
		super.onStop();
		/* Unregister the fragment */
		Rtcc.eventBus().unregister(this);
		if (getActivity().isChangingConfigurations()) {
			return;
		}

		Ui.setKeepScreenOn(getActivity().getWindow(), false);
		this.mFullScreenController.onStop();

		if (this.mCall != null && this.mCall.getStatus() == CallStatus.ACTIVE) {
			if (this.mCall.isSendingScreenShare()) {
				this.mScreenShareController.pause();
				this.mScreenShareController.stop();
			}
			if (this.mCall.isSendingVideo()) {
				this.mCall.videoStop();
				this.mCallViewController.pause();
			}
			this.mHasGoneBackground = true;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		this.mScreenShareController.saveState(outState);
		super.onSaveInstanceState(outState);
	}

	private void addButtonToWebView()
	{
		// Activity activity=getCallingActivity();
	}

	private void switchToWebView()
	{
		Intent intent=new Intent(getActivity(), MainActivity.class);
		//Intent intent=new Intent(getActivity(), Synsormed.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(intent);
         // getActivity().moveTaskToBack(true);
	}

	@Override
	public void onCallControlsClick(CallButton button) {
		if (this.mCallListener != null && (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED)) {
			this.mCallListener.onHangup(this.mCall);
			return;
		}

		boolean requestFullscreen = true;

		switch (button) {
		case PATIENT_INFO:
			Log.d("gunjot", "patient info clicked");
			callContainer.switchToWebView();
			break;
		case SWITCH:
			CallFragment.this.mCall.setVideoSource(CallFragment.this.mCall.getVideoOutSource() == VideoSource.FRONT ? VideoSource.BACK : VideoSource.FRONT);
			break;
		case FULLSCREEN:
			/* Immediately update the view in these cases */
			if (this.mCall.isWebRTC() || this.mCall.isConference() || !this.mCall.isReceivingVideo() || this.mCall.isReceivingScreenShare() || this.mCall.isSendingScreenShare()) {
				this.mFloatingWindowController.toggle();
				this.mCallViewController.update(false, true, true);
			} else {
				Contact contact = this.mCall.getContact(Contact.DEFAULT_CONTACT_ID);
				if (contact != null) {
					this.mCall.setInVideoProfile(contact.getVideoProfile() == VideoProfile.SD ? VideoProfile.HD : VideoProfile.SD);
				}
			}
			break;
		case HANGUP:
            Log.d("AminLog", "I am hanging up the call from the fragment");
			requestFullscreen = false;
			App.breadcrumb("Call.hangup()");
			this.mCall.hangup();
			break;
		case MICRO:
			if (this.mCall.isSendingAudio()) {
				this.mCall.audioStop();
			} else {
				this.mCall.audioStart();
			}
			this.mCallViewController.update(false, true, false);
			break;
		case SPEAKERS:
			this.mCall.setAudioRoute(this.mCall.getAudioRoute() == AudioRoute.SPEAKER ? AudioRoute.EARPIECE : AudioRoute.SPEAKER);
			this.mCallViewController.update(false, true, false);
			// switchToWebView();
			break;
		case VIDEO:
			if (this.mCall.isSendingVideo()) {
				this.mCall.videoStop();
			} else {
				this.mCall.videoStart();
			}
			break;
		case SCALE:
			this.mCall.setScaleMode(this.mCall.getScaleMode() == Call.ScaleMode.CROP ? Call.ScaleMode.FIT : Call.ScaleMode.CROP);
			break;
		case SHARE:
			if (this.mCall.isSendingScreenShare()) {
				this.mScreenShareController.stop();
			} else {
				if (!this.mFloatingWindowController.isFloating()) {
					this.mFloatingWindowController.setFloating(true);
					this.mCallViewController.update(false, true, true);
				}
				this.mScreenShareController.start();
			}
			break;
		case UNKNOWN:
		default:
			requestFullscreen = false;
			break;
		}
		if (requestFullscreen) {
			this.mFullScreenController.schedule();
		}
	}

	/**
	 * This method catches all {@link StatusEvent}
	 * 
	 * @param event
	 *            The delivered {@link StatusEvent}
	 */
	@RtccEventListener
	public void onCallStatusChanged(final StatusEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		if (event.getStatus() == CallStatus.ENDED && this.mCallListener != null) {
			this.mCallListener.onHangup(this.mCall);
		}
	}

	/**
	 * This method catches all {@link AuthenticatedEvent}
	 * 
	 * @param event
	 *            The delivered {@link AuthenticatedEvent}
	 */
	@RtccEventListener
	public void onCanCreateCallChanged(final AuthenticatedEvent event) {
		if (!event.isSuccess()) {
			/* Show the {@link ReconnectingDialogFragment} that can be cancelled */
			ReconnectingDialogFragment dialogFragment = (ReconnectingDialogFragment) getFragmentManager().findFragmentByTag(ReconnectingDialogFragment.class.getSimpleName());
			if (dialogFragment == null) {
				ReconnectingDialogFragment reconnectingDialog = ReconnectingDialogFragment.newInstance(this.mCall);
				reconnectingDialog.setTargetFragment(this, 1);
				reconnectingDialog.show(getFragmentManager(), ReconnectingDialogFragment.class.getSimpleName());
			}
		} else {
			/* Force resume the video if the call was interrupted by a PSTN call */
			if (this.mCall != null && this.mHasGoneBackground) {
				this.mHasGoneBackground = false;
				this.mCall.videoStart();
			}
		}
	}

	/**
	 * This method catches all {@link VideoInEvent}
	 * 
	 * @param event
	 *            The delivered {@link VideoInEvent}
	 */
	@RtccEventListener
	public void onCallReceivingVideoChanged(final VideoInEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		/* Fix the video quality according to the floating window flag */
		if (!this.mCall.isConference() && !this.mCall.isReceivingScreenShare() && event.isReceivingVideo()) {
			if (this.mCall.getVideoInProfile(Contact.DEFAULT_CONTACT_ID) == VideoProfile.HD && this.mFloatingWindowController.isFloating()) {
				this.mCall.setInVideoProfile(VideoProfile.SD);
			} else if (this.mCall.getVideoInProfile(Contact.DEFAULT_CONTACT_ID) == VideoProfile.SD && !this.mFloatingWindowController.isFloating()) {
				this.mCall.setInVideoProfile(VideoProfile.HD);
			}
		}

		this.mCallViewController.update(true, true, true);
	}

	/**
	 * This method catches all {@link ScreenShareInEvent}
	 * 
	 * @param event
	 *            The delivered {@link ScreenShareInEvent}
	 */
	@RtccEventListener
	public void onCallReceivingScreenShareChanged(final ScreenShareInEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(false, true, false);
		if (event.isReceivingScreenShare()) {
			this.mFloatingWindowController.setFloating(true);
		}
	}

	/**
	 * This method catches all {@link AudioRouteEvent}
	 * 
	 * @param event
	 *            The delivered {@link AudioRouteEvent}
	 */
	@RtccEventListener
	public void onCallCallAudioRouteChanged(final AudioRouteEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(false, true, false);
	}

	/**
	 * This method catches all {@link VideoOutEvent}
	 * 
	 * @param event
	 *            The delivered {@link VideoOutEvent}
	 */
	@RtccEventListener
	public void onCallSendingVideoChangedEvent(VideoOutEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(true, true, true);
	}

	/**
	 * This method catches all {@link ScreenShareOutEvent}
	 * 
	 * @param event
	 *            The delivered {@link ScreenShareOutEvent}
	 */
	@RtccEventListener
	public void onCallSendingScreenShareChangedEvent(ScreenShareOutEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}
		if (event.isSendingScreenShare()) {
			this.mScreenShareController.postStart();
		} else {
			this.mScreenShareController.postStop();
		}

		this.mCallViewController.update(true, true, true);
	}

	/**
	 * This method catches all {@link VideoInSizeEvent}
	 * 
	 * @param event
	 *            The delivered {@link VideoInSizeEvent}
	 */
	@RtccEventListener
	public void onCallVideoSizeChangedEvent(VideoInSizeEvent event) {
        Log.d("AminLog","VideoInSizeEvent is called");
		if (!event.isMatching(this.mCall)) {
			return;
		}
		// event is ignored in conference or if you are screen sharing
		if (this.mCall.isWebRTC() || this.mCall.isConference() || this.mCall.isSendingScreenShare()) {
			return;
		}

		switch (event.getVideoProfile()) {
		case HD:
            Log.d("AminLog","HD Video incoming");
			this.mFloatingWindowController.setFloating(false);
			break;
		case SD:
            Log.d("AminLog","SD Video incoming. Set Floating to false anyway");
			//this.mFloatingWindowController.setFloating(true);
            this.mFloatingWindowController.setFloating(false);
			this.mFullScreenController.schedule();
			break;
		default:
			break;
		}

		this.mCallViewController.update(true, true, true);
	}

	/**
	 * This method catches all {@link ScreenShareInSizeEvent}
	 * 
	 * @param event
	 *            The delivered {@link ScreenShareInSizeEvent}
	 */
	@RtccEventListener
	public void onCallScreenShareSizeChangedEvent(ScreenShareInSizeEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}
	}

	/**
	 * This method catches all {@link ParticipantEvent}
	 * 
	 * @param event
	 *            The delivered {@link ParticipantEvent}
	 */
	@RtccEventListener
	public void onCallConferenceParticipantChangedEvent(ParticipantEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(true, false, false);
	}

	/**
	 * This method catches all {@link ParticipantListEvent}
	 * 
	 * @param event
	 *            The delivered {@link ParticipantListEvent}
	 */
	@RtccEventListener
	public void onCallConferenceParticipantListEvent(ParticipantListEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(true, false, false);
	}

	/**
	 * This method catches all {@link FloorListEvent}
	 * 
	 * @param event
	 *            The delivered {@link FloorListEvent}
	 */
	@RtccEventListener
	public void onCallConferenceFloorListEvent(FloorListEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		this.mCallViewController.update(true, false, false);
	}

}
