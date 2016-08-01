package net.rtccloud.helper.fragment;

import net.rtccloud.helper.App;
import net.rtccloud.helper.MainActivity;
//import net.rtccloud.helper.R;
import net.rtccloud.helper.listener.OnLoginFragmentListener;
import net.rtccloud.helper.util.Scheme.Parameter;
import net.rtccloud.helper.util.Ui;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.RtccEngine.Status;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.event.global.ConnectedEvent;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.synsormed.mobile.R;

/**
 * This {@link Fragment} will be the first to be displayed when the application starts. It contains a form to login.<br />
 * There are 2 pre-filled fields:
 * <ul>
 * <li>{@link #mUserIdView}</li>
 * <li>{@link #mDisplayNameView}</li>
 * </ul>
 * When the user click the {@link #mLogInBtn}, it will start the initialization {@link Rtcc#initialize(String, android.content.Context)} and will automatically chain with the authentication
 * {@link RtccEngine#authenticate(android.content.Context, String, net.rtccloud.sdk.RtccEngine.UserType)}. The {@link OnLoginFragmentListener} callback is used to inform the host {@link MainActivity}
 * of the success of the authentication.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class LoginFragment extends Fragment implements OnClickListener {

	/** Tag to identify the {@link LoginFragment} */
	public static final String TAG = LoginFragment.class.getSimpleName();
	/** Tag to identify the token request */
	private static final String REQUEST_TOKEN = "token";

	/** Callback used to dispatch the {@link AuthenticatedEvent} */
	private OnLoginFragmentListener mListener;
	/** Request queue containing the token request */
	RequestQueue mRequestQueue;

	/** Value of the userID at the time of login */
	private String mUserId;
	/** Value of the displayName at the time of login */
	private String mDisplayName;

	/** The userID field */
	private EditText mUserIdView;
	/** The displayName field */
	private EditText mDisplayNameView;
	/** The login button */
	private Button mLogInBtn;

	/** Private flag reflecting the UI enabled state */
	private boolean mEnableUiState = true;
	/** Private flag reflicting if the process can be cancelled */
	private boolean mCanBeCancelled = false;

	/**
	 * Helper method to show a {@link LoginFragment}.
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use.
	 */
	public static void show(FragmentManager fm) {
		fm.beginTransaction().setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out, R.animator.card_flip_left_in, R.animator.card_flip_left_out).replace(R.id.form_container, LoginFragment.newInstance(), LoginFragment.TAG).commit();
	}

	/**
	 * Factory method. Creates a new instance of this fragment.
	 * 
	 * @return A new instance of {@link LoginFragment}.
	 */
	public static LoginFragment newInstance() {
		return new LoginFragment();
	}

	/**
	 * Required empty public constructor
	 */
	public LoginFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		this.mUserId = App.defaultUserID;
		this.mDisplayName = App.defaultDisplayName;
		this.mRequestQueue = Volley.newRequestQueue(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.mRequestQueue.cancelAll(REQUEST_TOKEN);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_login, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		this.mUserIdView = (EditText) view.findViewById(R.id.user_id);
		this.mUserIdView.setText(this.mUserId);

		this.mDisplayNameView = (EditText) view.findViewById(R.id.display_name);
		this.mDisplayNameView.setText(this.mDisplayName);

		this.mLogInBtn = (Button) view.findViewById(R.id.log_in_button);
		this.mLogInBtn.setOnClickListener(this);

		enableUi(this.mEnableUiState, this.mCanBeCancelled);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() != null && Rtcc.instance() != null && Rtcc.instance().getCurrentCall() != null) {
			getActivity().getActionBar().show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);

		Parameter p = Parameter.APP_ID;
		if (App.getScheme().contains(p) && App.getScheme().canUse(p)) {
			App.getScheme().use(p);
			initialize();
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
			this.mListener = (OnLoginFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnLoginFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mListener = null;
	}

	/**
	 * Attempt to login. It will either execute:
	 * <ul>
	 * <li>{@link Rtcc#initialize(String, android.content.Context)} if the {@link RtccEngine} is not initialized</li>
	 * <li>{@link RtccEngine#authenticate(android.content.Context, String, net.rtccloud.sdk.RtccEngine.UserType)}, if the {@link RtccEngine} is already initialized</li>
	 * </ul>
	 */
	public void login() {
		if (Rtcc.instance() == null) {
			initialize();
		} else {
			authenticate();
		}
	}

	/**
	 * Starts the initialization process
	 */
	public void initialize() {
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_initializing), true);
		App.breadcrumb("Rtcc.initialize(appId=%s)", App.defaultAppID);
		Rtcc.initialize(App.defaultAppID, getActivity());
	}

	/**
	 * Starts the authentication process
	 */
	public void authenticate() {
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_authentication), true, true);
		this.mUserId = this.mUserIdView.getText().toString();
		if (App.defaultWillUseAuth) {
			authenticateAuth();
		} else {
			authenticatedPoc();
		}
	}

	/**
	 * Login in auth mode:<br/>
	 * <b>Warning!</b><br/>
	 * This token request is only for the sake of simplicity.<br/>
	 * You should use a secured connection and must already be authenticated to a backend.
	 */
	private void authenticateAuth() {
		String url = App.defaultAuthUrl + "?uid=" + this.mUserId;
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, response.toString());
				String token = response.optString("token", null);
				RtccEngine instance = Rtcc.instance();
				if (instance != null) {
					App.breadcrumb("RtccEngine.authenticate(token=%s, userType=%s)", token, App.defaultUserType);
					instance.authenticate(getActivity(), token, App.defaultUserType);
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				VolleyLog.d(TAG, "Error: " + error.getMessage());
				onStatusUpdate(getString(R.string.app_name), error.toString(), false);
			}
		});
		jsonObjReq.setTag(REQUEST_TOKEN);
		this.mRequestQueue.add(jsonObjReq);
	}

	/**
	 * Login in POC mode
	 */
	private void authenticatedPoc() {
		App.breadcrumb("RtccEngine.authenticate(token=%s, userType=%s)", this.mUserId, App.defaultUserType);
		Rtcc.instance().authenticate(getActivity(), this.mUserId, App.defaultUserType);
	}

	/**
	 * Cancels the authenciation process by calling the {@link RtccEngine#disconnect()}.
	 */
	public void cancel() {
		onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_authentication_cancelled), false);
		this.mRequestQueue.cancelAll(REQUEST_TOKEN);
		if (Rtcc.getEngineStatus() != Status.UNDEFINED) {
			Rtcc.instance().disconnect();
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
	public void enableUi(boolean enable, boolean canCancel) {
		this.mEnableUiState = enable;
		this.mCanBeCancelled = canCancel;

		Ui.enableView(this.mEnableUiState, this.mUserIdView, this.mDisplayNameView);
		Ui.enableView(this.mEnableUiState || this.mCanBeCancelled, this.mLogInBtn);
		this.mLogInBtn.setText(!this.mEnableUiState && this.mCanBeCancelled ? R.string.action_cancel : R.string.action_log_in);
		this.mLogInBtn.setBackgroundResource(!this.mEnableUiState && this.mCanBeCancelled ? R.drawable.selectable_bg_red : R.drawable.selectable_bg_green);
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
		if (this.mListener != null) {
			this.mListener.onStatusUpdate(title, subtitle, showProgress);
		}
		if (Rtcc.getEngineStatus() == Status.AUTHENTICATED) {
			enableUi(false, canCancel);
		} else {
			enableUi(!showProgress, canCancel);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.log_in_button:
			if (this.mCanBeCancelled && !this.mEnableUiState) {
				cancel();
			} else {
				login();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * This method catches all {@link ConnectedEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link ConnectedEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link ConnectedEvent}
	 */
	@RtccEventListener
	public void onConnected(final ConnectedEvent event) {
		if (event.isSuccess()) {
			authenticate();
		} else {
			onStatusUpdate(getString(R.string.app_name), event.getError().description(), false);
		}
	}

	/**
	 * This method catches all {@link AuthenticatedEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link AuthenticatedEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link AuthenticatedEvent}
	 */
	@RtccEventListener
	public void onAuthenticated(final AuthenticatedEvent event) {
		if (event.isSuccess()) {
			App.defaultUserID = this.mUserId;
			this.mDisplayName = this.mDisplayNameView.getText().toString();
			App.defaultDisplayName = this.mDisplayName;
			Rtcc.instance().setDisplayName(this.mDisplayName);
			onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_authenticated), false);
			if (this.mListener != null) {
				this.mListener.onAuthenticated();
			}
		} else {
			onStatusUpdate(getString(R.string.app_name), event.getError().description(), false);
		}
	}

}
