package net.rtccloud.helper.fragment;

import java.text.DateFormat;
import java.util.Date;

import com.synsormed.mobile.R;

import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.helper.controller.StatusBarController.StatusBarAction;
import net.rtccloud.helper.listener.OnAuthenticatedFragmentListener;
import net.rtccloud.helper.util.Ui;
import net.rtccloud.helper.util.Scheme.Parameter;
import net.rtccloud.sdk.Call.StartingOptions;
import net.rtccloud.sdk.MeetingPoint;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.event.meetingpoint.ErrorEvent;
import net.rtccloud.sdk.event.meetingpoint.RequestEvent;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


/**
 * This {@link Fragment} will be the second to be displayed when the application starts, after a successful authentication. It contains a single field and allows to:
 * <ul>
 * <li>check the status with {@link #check(String)} method or {@link RtccEngine#getStatus(String)}</li>
 * <li>create a call with {@link #call(String, boolean)} method or {@link RtccEngine#createCall(String)}</li>
 * <li>logout with {@link #logout()} method or {@link RtccEngine#disconnect()}</li>
 * </ul>
 * 
 * Checking the status of a callID will display a status bar with a quick action, to either:
 * <ul>
 * <li>retry to check the status if the {@link #mContactId} was unreachable</li>
 * <li>call the {@link #mContactId} if it was reachable</li>
 * </ul>
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class AuthenticatedFragment extends Fragment implements OnClickListener, TextWatcher {

	/** Tag to identify the {@link AuthenticatedFragment} */
	public static final String TAG = AuthenticatedFragment.class.getSimpleName();

	/** Callback used to dispatch the call creation or the logout action */
	private OnAuthenticatedFragmentListener mListener;

	/** Value of the calleID at the time of call */
	private String mContactId;

	/** contactID field */
	private EditText mContactIdView;
	/** Logout button */
	private Button mLogOutBtn;
	/** Send message button */
	private Button mMessageBtn;
	/** Check button */
	private Button mCheckBtn;
	/** Call with video button */
	private Button mCallVideoBtn;
	/** Call without video button */
	private Button mCallAudioBtn;
	/** Call buttons container */
	private LinearLayout mCallBtns;

	/** Create Meeting Point button */
	private Button mMpCreateBtn;
	/** Broadcast Meeting Point button */
	private Button mMpBroadcastBtn;
	/** Host Meeting Point button */
	private Button mMpHostBtn;
	/** Join Meeting Point button */
	private Button mMpJoinBtn;
	/** Delete Meeting Point button */
	private Button mMpDeleteBtn;
	/** Clear Meeting Point button */
	private ImageButton mMpClearBtn;

	/** The current MeetingPoint */
	protected MeetingPoint mMeetingPoint;

	/** Private flag reflecting the UI enabled state */
	private boolean mEnableUiState = true;
	/** Private flag reflicting if the process can be cancelled */
	private boolean mCanBeCancelled = false;

	/**
	 * Helper method to show a {@link AuthenticatedFragment}.
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use.
	 */
	public static void show(FragmentManager fm) {
		fm.beginTransaction().setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out, R.animator.card_flip_left_in, R.animator.card_flip_left_out).replace(R.id.form_container, AuthenticatedFragment.newInstance(), AuthenticatedFragment.TAG).commit();
	}

	/**
	 * Factory method. Creates a new instance of this fragment.
	 * 
	 * @return A new instance of {@link AuthenticatedFragment}.
	 */
	public static AuthenticatedFragment newInstance() {
		return new AuthenticatedFragment();
	}

	/**
	 * Required empty public constructor
	 */
	public AuthenticatedFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		this.mContactId = App.defaultCalleeId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_authenticated, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.mContactIdView = (EditText) view.findViewById(R.id.contact_id);
		this.mContactIdView.setText(this.mContactId);
		this.mContactIdView.addTextChangedListener(this);

		this.mLogOutBtn = (Button) view.findViewById(R.id.log_out_button);
		this.mMessageBtn = (Button) view.findViewById(R.id.message_button);
		this.mCheckBtn = (Button) view.findViewById(R.id.check_button);
		this.mCallAudioBtn = (Button) view.findViewById(R.id.call_button_audio);
		this.mCallVideoBtn = (Button) view.findViewById(R.id.call_button_video);
		this.mCallBtns = (LinearLayout) view.findViewById(R.id.call_buttons);

		this.mMpCreateBtn = (Button) view.findViewById(R.id.mp_create);
		this.mMpClearBtn = (ImageButton) view.findViewById(R.id.mp_clear);
		this.mMpBroadcastBtn = (Button) view.findViewById(R.id.mp_broadcast);
		this.mMpHostBtn = (Button) view.findViewById(R.id.mp_host);
		this.mMpJoinBtn = (Button) view.findViewById(R.id.mp_join);
		this.mMpDeleteBtn = (Button) view.findViewById(R.id.mp_delete);

		this.mLogOutBtn.setOnClickListener(this);
		this.mMessageBtn.setOnClickListener(this);
		this.mCheckBtn.setOnClickListener(this);
		this.mCallVideoBtn.setOnClickListener(this);
		this.mCallAudioBtn.setOnClickListener(this);

		this.mMpCreateBtn.setOnClickListener(this);
		this.mMpClearBtn.setOnClickListener(this);
		this.mMpBroadcastBtn.setOnClickListener(this);
		this.mMpHostBtn.setOnClickListener(this);
		this.mMpJoinBtn.setOnClickListener(this);
		this.mMpDeleteBtn.setOnClickListener(this);

		boolean isInCall = Rtcc.instance() != null && Rtcc.instance().getCurrentCall() != null;
		enableUi(this.mEnableUiState && !isInCall, this.mCanBeCancelled && !isInCall);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() != null && Rtcc.instance() != null && Rtcc.instance().getCurrentCall() == null) {
			getActivity().getActionBar().show();
		}
		updateFormVisibility();
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);

		Parameter p1 = Parameter.CALLEE_ID;
		if (App.getScheme().contains(p1) && App.getScheme().canUse(p1)) {
			App.getScheme().use(p1);
			call(App.defaultCalleeId, true);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		/* Unregister the fragment */
		Rtcc.eventBus().unregister(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mListener = (OnAuthenticatedFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnAuthenticatedFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mListener = null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.log_out_button:
			logout();
			break;
		case R.id.message_button:
			message(this.mContactIdView.getText().toString());
			break;
		case R.id.check_button:
			check(this.mContactIdView.getText().toString());
			break;
		case R.id.call_button_video:
			call(this.mContactIdView.getText().toString(), true);
			break;
		case R.id.call_button_audio:
			call(this.mContactIdView.getText().toString(), false);
			break;

		case R.id.mp_create:
			createMeetingPoint();
			break;
		case R.id.mp_delete:
			deleteMeetingPoint();
			break;
		case R.id.mp_host:
			hostMeetingPoint();
			break;
		case R.id.mp_join:
			joinMeetingPoint();
			break;
		case R.id.mp_broadcast:
			broadcastMeetingPoint();
			break;
		case R.id.mp_clear:
			clearMeetingPoint();
			break;

		default:
			break;
		}
	}

	/**
	 * Update the button bar visibility with the corresponding form state.
	 */
	public void updateFormVisibility() {
		Ui.setVisibility(TextUtils.isEmpty(this.mContactIdView.getText()) ? View.GONE : View.VISIBLE, this.mCallBtns, this.mCallVideoBtn, this.mCallAudioBtn, this.mCheckBtn, this.mMessageBtn);

		Ui.setVisibility(this.mMeetingPoint == null ? View.VISIBLE : View.GONE, this.mMpCreateBtn);
		Ui.setVisibility(this.mMeetingPoint != null ? View.VISIBLE : View.GONE, this.mMpClearBtn);
		Ui.setVisibility(this.mMeetingPoint != null && this.mMeetingPoint.isHost() ? View.VISIBLE : View.GONE, this.mMpBroadcastBtn, this.mMpDeleteBtn, this.mMpHostBtn);
		Ui.setVisibility(this.mMeetingPoint != null && !this.mMeetingPoint.isHost() ? View.VISIBLE : View.GONE, this.mMpJoinBtn);
	}

	/**
	 * Send a message to the provided contactId.<br />
	 * It will execute the {@link RtccEngine#sendDataToContact(byte[], String)} method.
	 * 
	 * @param contactId
	 *            The contactId to send a message to.
	 * 
	 */
	public void message(String contactId) {
		Ui.hideSoftKeyboard(getActivity(), this.mContactIdView);
		this.mContactId = contactId;
		App.defaultCalleeId = this.mContactId;
		App.breadcrumb("RtccEngine.sendDataToContact(contact=%s)", this.mContactId);
		boolean result = Rtcc.instance().sendDataToContact(("Hi " + this.mContactId + ",<br/>This message is sent over data channel.<br/><br/>" + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date())).getBytes(), this.mContactId);
		onStatusUpdate(getString(R.string.app_name), getString(result ? R.string.msg_message_success : R.string.msg_message_fail, contactId), false);
	}

	/**
	 * Check the status of the provided contactId.<br />
	 * It will execute the {@link RtccEngine#getStatus(String)} method.
	 * 
	 * @param contactId
	 *            The contactId to check.
	 * 
	 */
	public void check(String contactId) {
		this.mContactId = contactId;
		App.defaultCalleeId = this.mContactId;
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_check, contactId), true);
		App.breadcrumb("RtccEngine.getStatus(contactID=%s)", contactId);
		Rtcc.instance().getStatus(contactId);
	}

	/**
	 * Call the provided contactId.<br />
	 * It will execute the {@link RtccEngine#createCall(String)} method.
	 * 
	 * @param contactId
	 *            The contactId to call.
	 * @param withVideo
	 *            Call with video.
	 */
	public void call(String contactId, boolean withVideo) {
		this.mContactId = contactId;
		App.defaultCalleeId = this.mContactId;
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call, contactId), true);
		StartingOptions opts = new StartingOptions.Builder().setVideoEnabled(withVideo).build();
		App.breadcrumb("RtccEngine.createCall(contactID=%s, options=%s)", contactId, opts);
		Rtcc.instance().createCall(contactId, opts);
	}

	/**
	 * Create a MeetingPoint.<br />
	 * It will display a {@link CreateMeetingPointDialogFragment}.
	 */
	private void createMeetingPoint() {
		CreateMeetingPointDialogFragment dialog = CreateMeetingPointDialogFragment.newInstance();
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), CreateMeetingPointDialogFragment.class.getSimpleName());
	}

	/**
	 * Delete the MeetingPoint.<br />
	 * It will execute the {@link MeetingPoint#delete()} method.
	 */
	private void deleteMeetingPoint() {
		if (this.mMeetingPoint != null) {
			App.breadcrumb("MeetingPoint.delete() {%s}", this.mMeetingPoint.getId());
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_deleting), true);
			this.mMeetingPoint.delete();
		}
	}

	/**
	 * Host the MeetingPoint.<br />
	 * It will execute the {@link MeetingPoint#host()} method.
	 */
	private void hostMeetingPoint() {
		if (this.mMeetingPoint != null) {
			App.breadcrumb("MeetingPoint.host() {%s}", this.mMeetingPoint.getId());
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_hosting), true);
			this.mMeetingPoint.host();
		}
	}

	/**
	 * Join the MeetingPoint.<br />
	 * It will execute the {@link MeetingPoint#join()} method.
	 */
	private void joinMeetingPoint() {
		if (this.mMeetingPoint != null) {
			App.breadcrumb("MeetingPoint.join() {%s}", this.mMeetingPoint.getId());
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_joining), true);
			this.mMeetingPoint.join();
		}
	}

	/**
	 * Broadcast the MeetingPoint to a contact through {@link RtccEngine#sendDataToContact(byte[], String)}.<br />
	 * It will display a {@link BroadcastMeetingPointDialogFragment}.
	 */
	private void broadcastMeetingPoint() {
		BroadcastMeetingPointDialogFragment dialog = BroadcastMeetingPointDialogFragment.newInstance(this.mMeetingPoint);
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), BroadcastMeetingPointDialogFragment.class.getSimpleName());
	}

	/**
	 * Clear the current MeetingPoint.
	 */
	private void clearMeetingPoint() {
		App.breadcrumb("MeetingPoint.clear() {%s}", this.mMeetingPoint == null ? "null" : this.mMeetingPoint.getId());
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_cleared), false);
		this.mMeetingPoint = null;
		updateFormVisibility();
	}

	/**
	 * Acquire a MeetingPoint (sent by the host with {@link RtccEngine#sendDataToContact(byte[], String)})
	 * 
	 * @param mpid
	 *            The corresponding MeetingPoint id
	 */
	public void acquireMeetingPoint(String mpid) {
		this.mMeetingPoint = Rtcc.instance().getMeetingPoint(mpid, false);
		App.breadcrumb("acquireMeetingPoint() {%s}", this.mMeetingPoint == null ? "null" : this.mMeetingPoint.getId());
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_acquired), false);
		updateFormVisibility();
	}

	/**
	 * Dispatch the logout action to the host of the Fragment.
	 */
	public void logout() {
		if (this.mListener != null) {
			this.mListener.onLogout();
		}
	}

	/**
	 * Helper method to update the UI state with the provided flags.
	 * 
	 * @param enable
	 *            Flag indicating the new UI state.
	 * @param canCancel
	 *            Flag indicating if the process can be cancelled.
	 */
	private void enableUi(boolean enable, boolean canCancel) {
		this.mEnableUiState = enable;
		this.mCanBeCancelled = canCancel;
		Ui.enableView(enable, this.mContactIdView, this.mMessageBtn, this.mCheckBtn, this.mCallVideoBtn, this.mCallAudioBtn, this.mMpCreateBtn, this.mMpDeleteBtn, this.mMpClearBtn, this.mMpHostBtn, this.mMpJoinBtn);
		Ui.enableView(enable || this.mMeetingPoint != null, this.mMpBroadcastBtn);
		Ui.enableView(this.mEnableUiState || this.mCanBeCancelled, this.mLogOutBtn);
		this.mLogOutBtn.setText(!this.mEnableUiState && this.mCanBeCancelled ? R.string.action_cancel : R.string.action_log_out);
	}

	/**
	 * @param title
	 * @param subtitle
	 * @param showProgress
	 * @see #onStatusUpdate(String, String, boolean, boolean)
	 */
	public void onStatusUpdate(String title, String subtitle, boolean showProgress) {
		onStatusUpdate(title, subtitle, showProgress, false);
	}

	/**
	 * Forward a status update to the {@link #mListener} callback and to the {@link #enableUi(boolean, boolean)} method.
	 * 
	 * @param title
	 *            The title to display in the {@link ActionBar}.
	 * @param subtitle
	 *            The subtitle to display in the {@link ActionBar}.
	 * @param showProgress
	 *            Whether or not to show a {@link ProgressBar} in the {@link ActionBar}.
	 * @param canCancel
	 *            Flag indicating if the process can be cancelled.
	 */
	public void onStatusUpdate(String title, String subtitle, boolean showProgress, boolean canCancel) {
		boolean isInCall = Rtcc.instance() != null && Rtcc.instance().getCurrentCall() != null;
		if (this.mListener != null) {
			this.mListener.onStatusUpdate(title, subtitle, showProgress || isInCall);
		}
		enableUi(!showProgress && !isInCall, canCancel && !isInCall);
	}

	/**
	 * This method catches all {@link StatusEvent}
	 * 
	 * @param event
	 *            The delivered {@link StatusEvent}
	 */
	@RtccEventListener
	public void onStatus(final net.rtccloud.sdk.event.global.StatusEvent event) {
		this.onStatusUpdate(getString(R.string.app_name), getString(event.canBeCalled() ? R.string.msg_reachable : R.string.msg_unreachable, event.getUid()), false);
		App.breadcrumb("onStatus() {%s}", event);
		if (this.mListener != null) {
			Bundle data = new Bundle();
			data.putString(Parameter.CALLEE_ID.key, event.getUid());
			this.mListener.onShowStatusBar(getString(event.canBeCalled() ? R.string.msg_reachable : R.string.msg_unreachable, event.getUid()), event.canBeCalled() ? StatusBarAction.CALL : StatusBarAction.RETRY, data);
		}
	}

	/**
	 * This method catches all {@link AuthenticatedEvent}
	 * 
	 * @param event
	 *            The delivered {@link AuthenticatedEvent}
	 */
	@RtccEventListener
	public void onAuthenticatedEvent(final AuthenticatedEvent event) {
		App.breadcrumb("onAuthenticatedEvent() {%s}", event);
		if (event.isSuccess()) {
			this.onStatusUpdate(getString(R.string.app_name), null, false, false);
		} else {
			switch (event.getError()) {
			case NETWORK_LOST:
				this.onStatusUpdate(getString(R.string.app_name), event.getError().description(), true, true);
				break;
			case SIP_NOK:
			default:
				this.onStatusUpdate(getString(R.string.app_name), event.getError().description(), false, false);
				if (this.mListener != null) {
					this.mListener.onLogout();
				}
				break;
			}
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
		App.breadcrumb("onCallStatusChanged() {%s}", event);
		switch (event.getStatus()) {
		case CREATED:
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_created), false);
			break;
		case PROCEEDING:
			/* Show a {@link CallingDialogFragment} */
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_proceeding), true);
			CallingDialogFragment callingDialog = CallingDialogFragment.newInstance(event.getCall());
			callingDialog.setTargetFragment(this, 1);
			callingDialog.show(getFragmentManager(), CallingDialogFragment.class.getSimpleName());
			break;
		case RINGING:
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ringing), true);
			break;
		case ACTIVE:
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_active), true);
			if (this.mListener != null) {
				this.mListener.onCallActive(event.getCall());
			}
			break;
		case ENDED:
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ended, event.getCall() == null ? "" : DateUtils.formatElapsedTime(event.getCall().getCallDuration() / 1000L)), false);
			break;
		case PAUSED:
			this.onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_paused), false);
			break;
		default:
			break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// ignore
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// ignore
	}

	@Override
	public void afterTextChanged(Editable s) {
		updateFormVisibility();
	}

	/**
	 * This method catches all {@link StatusEvent}
	 * 
	 * @param event
	 *            The delivered {@link StatusEvent}
	 */
	@RtccEventListener
	public void onMeetingPointEvent(final net.rtccloud.sdk.event.meetingpoint.StatusEvent event) {
		App.breadcrumb("onMeetingPointEvent() {%s}", event);
		switch (event.getType()) {
		case CREATED:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_created), false);
			this.mMeetingPoint = event.getMeetingPoint();
			// this.mMeetingPoint.mode(Mode.AUTO_ACCEPT);
			updateFormVisibility();
			break;
		case HOSTED:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_hosted), true);
			event.getMeetingPoint().call();
			break;
		case DELETED:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_deleted), false);
			this.mMeetingPoint = null;
			updateFormVisibility();
			break;
		case DETAILS:
		case STOPPED:
		case UPDATED:
		default:
			break;
		}
	}

	/**
	 * This method catches all {@link RequestEvent}
	 * 
	 * @param event
	 *            The delivered {@link RequestEvent}
	 */
	@RtccEventListener
	public void onMeetingPointRequestEvent(final RequestEvent event) {
		App.breadcrumb("onMeetingPointRequestEvent() {%s}", event);
		switch (event.getType()) {
		case ACCEPTED:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_accepted), true);
			App.breadcrumb("MeetingPoint.call() {%s}", event.getMeetingPoint());
			event.getMeetingPoint().call();
			break;
		case DENIED:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_denied), false);
			break;
		case CANCELLED:
		case INVITED:
		case JOINING:
			// Display popup only if the Conf is already hosted
			if (event.getMeetingPoint().isHosted()) {
				String title = String.format(getString(R.string.msg_mp_request), event.getDisplayName());
				onStatusUpdate(getString(R.string.app_name), null, false);
				new AlertDialog.Builder(getActivity()).setTitle(title).setPositiveButton(R.string.prompt_accept, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (AuthenticatedFragment.this.mMeetingPoint != null) {
							App.breadcrumb("MeetingPoint.accept(%s)", event.getUid());
							AuthenticatedFragment.this.mMeetingPoint.accept(event.getUid());
						}
					}
				}).setNegativeButton(R.string.prompt_deny, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (AuthenticatedFragment.this.mMeetingPoint != null) {
							App.breadcrumb("MeetingPoint.deny(%s)", event.getUid());
							AuthenticatedFragment.this.mMeetingPoint.deny(event.getUid());
						}
					}
				}).setCancelable(false).create().show();
			}
			break;
		case PENDING:
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_pending), false);
			break;
		default:
			break;
		}
	}

	/**
	 * This method catches all {@link ErrorEvent}
	 * 
	 * @param event
	 *            The delivered {@link ErrorEvent}
	 */
	@RtccEventListener
	public void onMeetingPointErrorEvent(final ErrorEvent event) {
		App.breadcrumb("onMeetingPointErrorEvent() {%s}", event);
		onStatusUpdate(getString(R.string.app_name), "Error: " + event.getData(), false);
	}
}
