package com.weemo.phonegap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

//import com.weemo.sdk.impl.MUCLConstants.UserType;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.telephony.TelephonyManager;
/*
import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.WeemoEngine.UserType;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallCreatedEvent;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.call.ReceivingVideoChangedEvent;
import com.weemo.sdk.event.global.AuthenticatedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.ConnectedEvent;
import com.weemo.sdk.event.global.StatusEvent;
import com.weemo.sdk.impl.MUCLConstants;
import com.weemo.phonegap.CallFragment.TouchType;
import com.weemo.phonegap.floating.FloatingWindow;
*/
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.weemo.phonegap.RtccCallFragment;
import com.weemo.phonegap.RtccCallActivity;

import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.call.VideoInEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.event.global.ConnectedEvent;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.RtccEngine.UserType;
import com.weemo.phonegap.CallContainer;

import com.quickblox.core.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.*;
import com.quickblox.users.model.QBUser;
import com.quickblox.users.QBUsers;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCStatsReportCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;

import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;




/**
 *	Core plugin of the app.
 */
public class RtccAndroidPhonegap extends CordovaPlugin implements QBRTCClientSessionCallbacks, QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks{

	/**	Callback linked to connection events */
	private CallbackContext connectionCallback = null;
	/**	Callback linked to authentication events */
	private CallbackContext authenticationCallback = null;
	/**	Callback linked to callWindow events */
	private CallbackContext callWindowCallback = null;
    private CallbackContext createCallCallback = null;
	private  static Boolean isProvider=false;
	private  static Boolean isDataAvailable=false;

	/**	Callback map containing their status */
	private Map<String, CallbackContext> statusCallbacks = new HashMap<String, CallbackContext>();

	/** The AudioManager allows us to detect the speaker mode and the wired headset mode */
	private AudioManager audioManager;
	
	static final String APP_ID = "41633";
	static final String AUTH_KEY = "YKQGUMXRvwtK9kf";
	static final String AUTH_SECRET = "ND-eQkQxAYAUYpM";
	static final String ACCOUNT_KEY = "Ky2eW7fR2tqfoDzxgZB1";
	private QBChatService chatService;
	private QBRTCClient rtcClient;
	private QBRTCSession currentSession;
    private int currentPatientID;
    private boolean hasRemoteTrack = false;
    private boolean hasLocalTrack = false;
    private static QBRTCVideoTrack currentLocalTrack =  null;
    private static QBRTCVideoTrack currentRemoteTrack = null;
		

	/**	Custom Exception */
	@SuppressWarnings("serial")
	private static class DirectError extends Exception {
		/** The code value */
		int code;

		/**
		 * Default constructor with supplied code
		 * @param code .
		 */
		public DirectError(int code) {
			this.code = code;
		}

		/** @return the code value */
		public int getCode() {
			return code;
		}
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		/*Rtcc.ensureNativeLoad();

		Rtcc.eventBus().register(this);

		audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);*/
	}


	@Override
	public void onDestroy() {
         try {

			 if (Rtcc.getEngineStatus() != RtccEngine.Status.UNDEFINED.UNDEFINED) {
				 Log.d("AminLog", "About to disconnect RTCC on destroy");
				 Rtcc.instance().disconnect();
			 }
			 Rtcc.eventBus().unregister(this);
			 super.onDestroy();
		 }catch (ExceptionInInitializerError e){
			 e.printStackTrace();
		 }
	}

	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callback) throws JSONException {
		Log.d("AminLog", "The string action is: " + action);
		Log.d("call_session", "The string action is: " + action);
		try {
			Log.d("call_session", " entered try");
			if ("initQB".equals(action))
				initQB(callback, args.getString(0));
			else if ("authent".equals(action)){	
				authent(callback, args.getString(0));
			}
			else if ("acceptCall".equals(action)){
				acceptCall(callback);
			}
			else if ("setDisplayName".equals(action))
				setDisplayName(callback, args.getString(0));
			else if ("getStatus".equals(action))
				getStatus(callback, args.getString(0));
			else if ("createCall".equals(action))
			{
				isProvider=args.getBoolean(1);
				isDataAvailable=args.getBoolean(2);
				if(args.getBoolean(1))
				{
					Log.d("provider", "video screen is in use by provider");
				}
				else
				{
					Log.d("provider", "Video Screen is in use by patient.");
				}
				if(args.getBoolean(2))
				{
					Log.d("provider", "patient data is available");
				}
				else
				{
					Log.d("provider", "patient data is not available.");
				}
				createCall(callback, args.getString(0));
			}
			else if ("disconnect".equals(action))
				disconnect(callback);
			else if ("muteOut".equals(action))
				muteOut(callback, args.getInt(0), args.getBoolean(1));
			else if ("resume".equals(action))
			{
				if(args.getBoolean(1))
				{
					Log.d("patientInResume", "video screen is in use by provider");
				}
				else
				{
					Log.d("patientInResume", "Video Screen is in use by patient.");
				}
				isProvider=args.getBoolean(1);
				resume(callback, args.getInt(0));
			}
				
			else if ("hangup".equals(action)){
				Log.d("call_session", "hangup short entered");
				hangup(callback, args.getInt(0));
			}

			else if ("hangUp".equals(action)){
				Log.d("call_session", "hangUp entered");
				final Activity activity = cordova.getActivity();
				final CallbackContext callbackLocal = callback;
				final CordovaArgs argsLocal = args;

				int callId = 0;
				try
				{
					callId = argsLocal.getInt(0);
				}
				catch(Exception e){
					Log.d("call_session", "argsLocal callid issue");
				}

				final int  callIdFinal = callId;
				activity.runOnUiThread(new Runnable() {
					@Override public void run() {
						hangup(callbackLocal, callIdFinal);
					}
				});
			}

			else if ("displayCallWindow".equals(action))
			{
				// displayCallWindow(callback, args.getInt(0), args.getBoolean(1));
				addCallContainer(args.getInt(0));
			}
			else if ("displayCallView".equals(action))
				displayCallView(callback, args.getInt(0));
			else if ("hideCallView".equals(action))
				hideCallView(callback);
			else if ("setAudioRoute".equals(action))
				setAudioRoute(callback, args.getBoolean(0));
			else if ("getOSInfos".equals(action))
				getOSInfos(callback);
            else if ("getEngineStatus".equals(action))
                getEngineStatus(callback);
            else if ("getNetworkType".equals(action)){
				getNetworkType(callback);
			}
			else
				return false;
		}
		catch (DirectError e) {
			Log.d("call_session", "DirectError =?>" + e);
			callback.error(e.getCode());
		}
		return true;
	}

	
	private void getNetworkType(CallbackContext callback){
		TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		switch (tm.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_LTE:
				Log.d("AminLog", ">>>> Network type is LTE");
				callback.success("LTE");
				break;
			default:
				Log.d("AminLog", ">>>>> Network type is NOT LTE");
				callback.success("OTHER");
				break;

		}
	}

    public QBRTCVideoTrack getCurrentRemoteTrack(){
        return currentRemoteTrack;
    }

    public QBRTCVideoTrack getCurrentLocalTrack(){
        return currentLocalTrack;
    }



	public static Boolean isProvider()
	{
		return isProvider;
	}


	public static Boolean isDataAvailable()
	{
		return isDataAvailable;
	}

	private void addCallContainer(int callId)
	{
		CallContainer callContainer=new CallContainer(cordova.getActivity(), callId);

	}


    private void getEngineStatus(CallbackContext callback) throws DirectError{

        //RtccEngine rtcc = Rtcc.instance();
		//RtccEngine.Status status = Rtcc.getEngineStatus();
        RtccEngine.Status mystatus = Rtcc.getEngineStatus();
        switch (mystatus){
            case UNDEFINED:
                callback.success("Undefined");
                break;
            case CONNECTED:
                callback.success("Connected");
                break;
            case AUTHENTICATED:
                callback.success("Authenticated");
                break;
        }

        //if(rtcc == null){
        //    Log.d("AminLog","Engine not initialized when checking Engine Status");
        //}


    }
    
    	/**
	 * Initialise the Weemo engine 
	 * @param callback The callback to notify
	 * @param appId The Weemo APPlication IDentifier
	 */
	private void initQB(CallbackContext callback, String appId) throws DirectError{

		connectionCallback = callback;
        
        Log.d("AminLog", "I made it to initQB");
		Log.d("call_session", "initQB");
        
		QBSettings.getInstance().init(this.cordova.getActivity().getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
		QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
		callback.success();
/*
        if(Rtcc.instance() == null){
            Log.d("AminLog", "The instance is null in init method");
        }

        if(Rtcc.getEngineStatus() == RtccEngine.Status.UNDEFINED || Rtcc.instance() == null){
            Log.d("AminLog","Engine not initialized so about to initialize");
            Rtcc.initialize(appId, cordova.getActivity());
            if(Rtcc.getEngineStatus() == RtccEngine.Status.UNDEFINED){
                Log.d("AminLog","Ran Rtcc Init but engine still undefined");
                callback.success();
            }else{
                Log.d("AminLog","Ran Rtcc Init and was successful :" + Rtcc.getEngineStatus());
                //callback.success();
            }
        }else{
            Log.d("AminLog","RTCC engine is already initialized");
            callback.success();
        }
        */

	}

	/**
	 * Initialise the Weemo engine 
	 * @param callback The callback to notify
	 * @param appId The Weemo APPlication IDentifier
	 */
	private void initialize(CallbackContext callback, String appId) throws DirectError{

        connectionCallback = callback;

        if(Rtcc.instance() == null){
            Log.d("AminLog", "The instance is null in init method");
        }

        if(Rtcc.getEngineStatus() == RtccEngine.Status.UNDEFINED || Rtcc.instance() == null){
            Log.d("AminLog","Engine not initialized so about to initialize");
            Rtcc.initialize(appId, cordova.getActivity());
            if(Rtcc.getEngineStatus() == RtccEngine.Status.UNDEFINED){
                Log.d("AminLog","Ran Rtcc Init but engine still undefined");
                callback.success();
            }else{
                Log.d("AminLog","Ran Rtcc Init and was successful :" + Rtcc.getEngineStatus());
                //callback.success();
            }
        }else{
            Log.d("AminLog","RTCC engine is already initialized");
            callback.success();
        }

	}

	/**
	 * @return The {@link RtccEngine} instance
	 * @throws DirectError error
	 */
	private RtccEngine _getEngine() throws DirectError {
		Log.d("AminLog","I am about to get the Rtcc engine");
		RtccEngine rtcc = Rtcc.instance();
		if (rtcc == null)
			throw new DirectError(-1);
		return rtcc;
	}

	/**
	 * Connects to servers with given userID.
	 * @param callback The callback to notify
	 * @param token The token that authenticate this user
	 * @param type The type of the user
	 * @throws DirectError error
	 */
	private void authent(CallbackContext callback, String userID) throws DirectError {
		Log.d("AminLog","I am about to authenticate");
		Log.d("call_session", "authent");
        authenticationCallback = callback;
        loginQBUser(userID);

	}
	/**
	*
	* Log in the QB user
	*
	**/
	
	private void loginQBUser(String loginID){
	
		String login = loginID;
		String password = login.concat("password"); 
 
		final QBUser user = new QBUser(login, password);
 
		// CREATE SESSION WITH USER
		// If you use create session with user data,  
		// then the user will be logged in automatically
		QBAuth.createSession(login, password, new QBEntityCallback<QBSession>() {
		   @Override
		   public void onSuccess(QBSession session, Bundle bundle) {
		   
		   		Log.d("AminLog","Successfully created auth session in QB");
 
			  user.setId(session.getUserId());                
 
			  // INIT CHAT SERVICE
			  chatService = QBChatService.getInstance();
 
			  // LOG IN CHAT SERVICE
			  chatService.login(user, new QBEntityCallback<QBUser>() {
 
				 @Override
				 public void onSuccess(QBUser result, Bundle bundle) {
					// success
					Log.d("AminLog","Successfully logged into QB chat");
					//Now that we are successfully logged in, init the RTCClient
					initQBRTCClient();
					authenticationCallback.success();
				 }
 
				 @Override
				 public void onError(QBResponseException errors) {
					//error
					Log.d("AminLog","Failed logging into QB chat");
					String errorString = "";
					for (String s : errors.getErrors())
						{
    					errorString += s + "\t";
						}
					authenticationCallback.error(errorString);
				 }
			  });
		   }
 
		   @Override
		   public void onError(QBResponseException errors) {
			  //error
			  Log.d("AminLog","Failed created auth session in QB. Try to register new user");
			  String errorString = "";
			  for (String s : errors.getErrors())
						{
    					errorString += s + "\t";
						}
				registerQBUser(user);
		   }
		});
	
	}
	/**
	*
	*Use this to register a new QB user if user not found during login
	*
	*/
	private void registerQBUser(QBUser user){
	
		final QBUser myuser = user;
		QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
			@Override
			public void onSuccess(QBSession session, Bundle params) {
				
				try{
					//If session was created, try to register new user
					QBUsers.signUp(myuser);
					//If registration was successful, retry logging in with user
					loginQBUser(myuser.getLogin());
				}catch(QBResponseException errors){
									
					Log.d("AminLog","Failed signing up new user");
					String errorString = "";
					for (String s : errors.getErrors())
					{
						errorString += s + "\t";
					}
					authenticationCallback.error(errorString);
					
				}
				
			}

			@Override
			public void onError(QBResponseException errors) {
				// errors
				Log.d("AminLog","Failed creating session to register user user");
				String errorString = "";
				for (String s : errors.getErrors())
				{
					errorString += s + "\t";
				}
				authenticationCallback.error(errorString);
				}
		});

	
	}
	
	
	private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this.cordova.getActivity().getApplicationContext());
        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });
/*
        rtcClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.longToast(s);
                    }
                });
            }
        });
*/

        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setStatsReportInterval(60);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        
        rtcClient.addConnectionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();

        /*QBChatService.getInstance().addConnectionListener(new AbstractConnectionListener() {

            @Override
            public void connectionClosedOnError(Exception e) {
                showNotificationPopUp(R.string.connection_was_lost, true);
            }

            @Override
            public void reconnectionSuccessful() {
                showNotificationPopUp(R.string.connection_was_lost, false);
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.i(TAG, "reconnectingIn " + seconds);
            }
        });
        */
    }
    
    private void acceptCall(CallbackContext callback) throws DirectError
    {
    
    	displayCallWindow(callback, 0, false);
    	
    	callback.success();
    
    }

	/**
	 * Set the display name used by the application for the authenticated user
	 * @param callback The callback to notify
	 * @param displayName the name
	 * @throws DirectError error
	 */
	private void setDisplayName(CallbackContext callback, String displayName) throws DirectError {
		RtccEngine rtcc = _getEngine();
		rtcc.setDisplayName(displayName);
		callback.success();
	}

	/**
	 * Check if a user can be called.
	 * @param callback The callback to notify
	 * @param userID The ID of the contact to check
	 * @throws DirectError error
	 */
	private void getStatus(CallbackContext callback, String userID) throws DirectError {
		RtccEngine rtcc = _getEngine();
		statusCallbacks.put(userID, callback);
		rtcc.getStatus(userID);
	}

	private void findQBIDFor(String login){

		try {

			QBUser thePatient = QBUsers.getUserByLogin(login);
			Log.d("AminLog","I found the patient ID: " + thePatient.getId());
            currentPatientID = thePatient.getId();
		} catch (QBResponseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates a call whose recipient is contactID.
	 * @param callback The callback to notify
	 * @param userID The ID of the contact or the conference to call
	 * @throws DirectError error
	 */
	private void createCall(CallbackContext callback, String userID) throws DirectError {
		/*RtccEngine rtcc = _getEngine();
		rtcc.createCall(userID);*/

        createCallCallback = callback;

		Log.d("tracks","createCall userID: " + userID);
		QBRTCTypes.QBConferenceType qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
        findQBIDFor(userID);

        Log.d("tracks","I am about to try to call: " + currentPatientID);

        //Initiate opponents list
        List<Integer> opponents = new ArrayList<Integer>();
		opponents.add(currentPatientID);


        //Init session
        QBRTCSession session =
                QBRTCClient.getInstance(this.cordova.getActivity().getApplicationContext()).createNewSessionWithOpponents(opponents, qbConferenceType);



        currentSession = session;

//        currentSession.addVideoTrackCallbacksListener(this);

        webView.sendJavascript("videoPlugin.internal.callCreated('0', 'PROCEEDING');");

        //webView.loadUrl("javascript:videoPlugin.internal.callCreated('0', 'PROCEEDING');");
		callback.success();
		displayCallWindow(createCallCallback, 0, false);
	}



	/**
	 * @param callback The callback to notify
	 */
	private void disconnect(CallbackContext callback) throws DirectError {

        if(Rtcc.getEngineStatus() != RtccEngine.Status.UNDEFINED){
            Log.d("AminLog","I am in java disconnect");
			Log.d("call_session","I am in java disconnect");
            Rtcc.instance().disconnect();

        }
		callback.success();
	}

	/**
	 * Get a current call by its id
	 * @param callID The ID of the call to get
	 * @return The call if found or throws a {@link DirectError}
	 * @throws DirectError error
	 */
	private Call _getCall(int callID) throws DirectError {
		Call mycall = _getEngine().getCall(callID);
		if (mycall == null)
			throw new DirectError(-2);
		return mycall;
	}

	/**
	 * Mute or un-mute the call
	 * @param callback The callback to notify
	 * @param callID The ID of the call to mute
	 * @param mute whether the call should be muted
	 * @throws DirectError error
	 */
	private void muteOut(CallbackContext callback, int callID, boolean mute) throws DirectError {
		Call mycall = _getCall(callID);
		if (mute)
			mycall.muteAll(true);
		else
			mycall.muteAll(false);
		callback.success();
	}

	/**
	 * Resume the call if it was paused. Pick it up if it is ringing.
	 * @param callback The callback to notify
	 * @param callID The ID of the call to resume
	 * @throws DirectError error
	 */
	private void resume(CallbackContext callback, int callID) throws DirectError {
		Call mycall = _getCall(callID);
		mycall.resume();
		callback.success();
	}

	/**
	 * Hang up the call and stop it
	 * @param callback The callback to notify
	 * @param callID Resume the call if it was paused. Pick it up if it is ringing.
	 * @throws DirectError error
	 */

	private void hangup(CallbackContext callback, int callID){

		/*Log.d("call_session", "hangup in hangup");
		try{
			Log.d("call_session", "hangup");

			if(callID != 0){
				removeCallFragment();
				Call mycall = _getCall(callID);
				if(mycall.getStatus() != CallStatus.ENDED){
					mycall.hangup();
				}
			}

			clearSession();
			clearLocalSession();
			callback.success();
		}
		catch(Exception e){
			Log.d("call_session", "hangup exceptin -->" + e);
		}*/

        if(currentSession != null){
            currentSession.hangUp(null);
        }
        callback.success();

	}

	private void clearSession(){
		rtcClient.getInstance(this.cordova.getActivity().getApplicationContext()).destroy();

		//Log out of chat service
		QBChatService.getInstance().destroy();
	}

	private static void clearLocalSession(){
		isProvider = false;
	}


	
	/**
	 * Remove the {@link CallFragment} from the Activity if there is one
	 */
	@SuppressLint("NewApi") protected void removeCallFragment(){
		final Activity activity = cordova.getActivity();
		final int id = activity.getResources().getIdentifier("weemo_fragment_layout", "id", activity.getPackageName());
		RtccCallFragment fragment = (RtccCallFragment) activity.getFragmentManager().findFragmentById(id);
		if(fragment != null) {
			activity.getFragmentManager().popBackStack("CallFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	/**
	 * Display a full-screen call window
	 * @param callback The callback to notify
	 * @param callId The ID of the call to resume
	 * @param canComeBack error
	 */
	private void displayCallWindow(CallbackContext callback, final int callId, final boolean canComeBack) {
		final Activity activity = cordova.getActivity();
		callWindowCallback = callback;
		Log.d("AminLog", "Amin is in the display Call Window");
		final String sessionID = currentSession.getSessionID();
		activity.runOnUiThread(new Runnable() {
			@Override public void run() {
				//Remove CallFragment if it already exists
				//removeCallFragment();
				
				//Intent intent = new Intent(cordova.getActivity(), RtccCallActivity.class);
				Intent intent = new Intent(cordova.getActivity(), com.quickblox.sample.groupchatwebrtc.activities.CallActivity.class);
				intent.putExtra("canComeBack", canComeBack);
				intent.putExtra("callId", sessionID);
                intent.putExtra("isProvider",isProvider);
				cordova.startActivityForResult(RtccAndroidPhonegap.this, intent, 2142);
			}
		});
		callback.success();
	}

	/**
	 * Display a {@link FloatingWindow} in the current Activity 
	 * @param callback The callback to notify
	 * @param callId The ID of the call to resume
	 */
	@SuppressLint("NewApi") private void displayCallView(CallbackContext callback, final int callId) {
		
		
		final Activity activity = cordova.getActivity();
		final Resources res = activity.getResources();
		final int id = res.getIdentifier("weemo_fragment_layout", "id", activity.getPackageName());

		activity.runOnUiThread(new Runnable() {
			@Override public void run() {
				
				RtccCallFragment fragment = (RtccCallFragment) activity.getFragmentManager().findFragmentById(id);
				if(fragment == null) {
					FrameLayout mainLayout = (FrameLayout) activity.findViewById(android.R.id.content);
					FrameLayout temp = (FrameLayout) mainLayout.findViewById(id);
					if (temp == null) {
						temp = new FrameLayout(activity);
						LayoutParams params = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
						temp.setLayoutParams(params);
						temp.setId(id);
						mainLayout.addView(temp);
					}
/*
					activity.getFragmentManager()
						.beginTransaction()
						.add(id, CallFragment.newInstance(callId, TouchType.SLIDE_CONTROLS, res.getInteger(res.getIdentifier("camera_correction", "integer", activity.getPackageName())), true))
						.addToBackStack("CallFragment")
						.commit();
						*/
				} else {
					removeCallFragment();
				}
			}
		});

		//Rtcc.eventBus().register(this);

		callback.success();
	}

	/**
	 * Remove the {@link CallFragment} from the Activity
	 * @param callback The callback to notify
	 */
	private void hideCallView(CallbackContext callback) {
		final Activity activity = cordova.getActivity();
		final int id = activity.getResources().getIdentifier("weemo_fragment_layout", "id", activity.getPackageName());

		activity.runOnUiThread(new Runnable() {
			@Override public void run() {
				removeCallFragment();

				FrameLayout layout = (FrameLayout) activity.findViewById(id);
				if (layout == null)
					return ;
				((ViewGroup)layout.getParent()).removeView(layout);

			}
		});

		if (callback != null)
			callback.success();
	}

	/**
	 * Event that is fired when the CallStatus of a call has changed.
	 * @param e the event
	 
	@RtccEventListener
	public void onCallViewCallStatusChanged(CallStatusChangedEvent e) {
		if (e.getCallStatus() != WeemoCall.CallStatus.ENDED)
			return ;

		hideCallView(null);

		//Weemo.eventBus().unregister(this);
	}
*/
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.i("onActivityResult", "onActivityResult " + requestCode + " " + resultCode);
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == 2142 && callWindowCallback != null) {
			callWindowCallback.success();
			callWindowCallback = null;

		}
	}

	/**
	 * Change the audio route (speakers or headphone)
	 * @param callback The callback to notify
	 * @param speakers speaker or headphone
	 */
	private void setAudioRoute(CallbackContext callback, boolean speakers) {
		audioManager.setSpeakerphoneOn(speakers);
		webView.sendJavascript("Weemo.internal.audioRouteChanged(" + (speakers ? "true" : "false") + ")");
		callback.success();
	}

	/**
	 * Send OS infos to the callback
	 * @param callback The callback to notify
	 * @throws JSONException exception
	 */
	private void getOSInfos(CallbackContext callback) throws JSONException {
		JSONObject infos = new JSONObject();

		infos.put("OS", "Android");
		infos.put("version", Build.VERSION.RELEASE);

	    DisplayMetrics metrics = new DisplayMetrics();
		cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		float density  = cordova.getActivity().getResources().getDisplayMetrics().density;
	    float dpWidth  = metrics.widthPixels / density;
	    infos.put("deviceType", dpWidth < 600 ? "phone" : (dpWidth < 720 ? "tablet-small" : "tablet-big"));

	    infos.put("screenWidth", metrics.widthPixels);
	    infos.put("screenHeight", metrics.heightPixels);
	    
	    Log.d("AminLog", "The metrics are, width: " + metrics.widthPixels + " and height: " + metrics.heightPixels);

	    callback.success(infos);
	}

    /**
     * Event that is fired when there is a VideoinEvent from a call
     * @param event VideoInEvent
     * Amin Holmes 12/12/14
     */


    @RtccEventListener
    public void onVideoInChanged(final VideoInEvent event) throws DirectError{

        Log.d("AminLog","I am in the VideoInEvent Listener");
        /*RtccEngine rtcc = _getEngine();
        Call myCall = rtcc.getCurrentCall();

        if(myCall != null) {
            ArrayList<Contact> mycontacts = myCall.getContacts();
            int contactID = mycontacts.get(0).getId();
            Call.VideoProfile theProfile = myCall.getVideoInProfile(contactID);
            Log.d("AminLog", "The VideoInProfile before settting is: " + theProfile);
            myCall.setInVideoProfile(Call.VideoProfile.HD);
            theProfile = myCall.getVideoInProfile(contactID);
            Log.d("AminLog", "The VideoInProfile after settting is: " + theProfile);
        }*/

    }

	/**
	 * Event that is fired when the connection / initialization has succeded or failed
	 * @param e event
	 */
	

	@RtccEventListener
	public void onCallStatusChanged(final net.rtccloud.sdk.event.call.StatusEvent event) {
        int callID = 0;

		switch (event.getStatus()) {
		case CREATED:
			Log.d("AminLog","Call status changed to: created");
		    callID  = event.getCall().getCallId();
			Log.d("AminLog", "The Call ID is: " + callID);
			String callDisplayName = event.getCall().getContactDisplayName(0);
			webView.sendJavascript("Weemo.internal.callCreated(" + callID + ", \'" + event.getStatus() + "\');");
			break;
		case ACTIVE:
			break;
		case RINGING:
		case PROCEEDING:
			break;	
		case ENDED:
			break;
		default:
			break;
		}
		
		webView.sendJavascript("Weemo.internal.callStatusChanged(" + callID + ", \'" + event.getStatus() + "\');");
		/*webView.sendJavascript("javascript: angular.element(document.querySelector('#pageContainer')).scope().$broadcast('callchange',\'" + event.getStatus() + "\');");*/
		
	}
	
	
	@RtccEventListener
	public void onConnected(final ConnectedEvent e) {

        Log.i("AminLog", "I am in the onconnected event");

		if (connectionCallback == null) {
            Log.i("AminLog", "The connection callback was null");
            return;
        }
		if (e.getError() == null) {
            Log.i("AminLog", "Connection was successful, sending callback");
            connectionCallback.success();
        }else{
            if(e.getError() == e.getError().CLOSED){
                Log.d("AminLog","In java plugin, connection is being closed");
            }
            connectionCallback.error(e.getError().code());
        }
		connectionCallback = null;
	}

	/**
	 * Event that is fired when the authentication has succeded or failed
	 * @param e event
	 */
	@RtccEventListener
	public void onAuthenticated(AuthenticatedEvent e) {
		if (authenticationCallback == null)
			return ;

		if (e.getError() == null)
			authenticationCallback.success();
		else
			authenticationCallback.error(e.getError().code());

		authenticationCallback = null;
	}

	/**
	 * Event that is fired in response to a {@link WeemoEngine#getStatus(String)} request
	 * @param e event
	 */
	@RtccEventListener
	public void onStatus(net.rtccloud.sdk.event.global.StatusEvent e) {
		CallbackContext callback = statusCallbacks.get(e.getUid());
		if (callback == null)
			return ;

		callback.success(e.canBeCalled() ? 1 : 0);

		authenticationCallback = null;
	}

	/**
	 * Event that is fired when a new call is created.
	 * @param e event
	 */
	@RtccEventListener
	
	
	public void onCallCreated(net.rtccloud.sdk.event.call.CreatedEvent e) {
	  //webView.sendJavascript("Weemo.internal.callCreated(" + e.getCall().getCallId() + ", \'" + e.getCall().getContactDisplayName().replace("'", "\\'") + "\');");
	  Log.d("Amin Log", "I am in the oncallcreated function");
	  //webView.sendJavascript("Weemo.internal.callCreated(" + e.getCall().getCallId() + ",\'AminDisplayName\')");
	//webView.sendJavascript("alert('from native side');");
	}

	/**
	 * Event that is fired when the CallStatus of a call has changed.
	 * @param e event
	 
	@SuppressWarnings("deprecation")
	@RtccEventListener
	public void onCallStatusChanged(StatusEvent e) {
		webView.sendJavascript("Weemo.internal.callStatusChanged(" + e.getCall().getCallId() + ", " + e.getStatus().getCode() + ");");
		if (e.getCallStatus() == CallStatus.PROCEEDING) {
			boolean speakers = !audioManager.isWiredHeadsetOn();
			audioManager.setSpeakerphoneOn(speakers);
			webView.sendJavascript("Weemo.internal.audioRouteChanged(" + (speakers ? "true" : "false") + ");");
		}
	}
*/
	/**
	 * Event that is fired when the {@link WeemoEngine#canCreateCall()} is down or back up
	 * @param e event
	 
	@RtccEventListener
	public void onCanCreateCallChanged(CanCreateCallChangedEvent e) {
		webView.sendJavascript("Weemo.internal.connectionChanged(" + (e.getError() != null ? e.getError().code() : 0) + ");");
	}
*/

	/**
	 * Event fired when you are now receiving video or not anymore
	 * @param e event
     */
	/*
	@RtccEventListener
	public void onReceivingVideoChanged(net.rtccloud.sdk.event.call.VideoInEvent e) {
		Log.i("JAVASCRIPT", "Weemo.internal.videoInChanged(" + e.getCall().getCallId() + ", " + (e.isReceivingVideo() ? "true" : "false") + ");");
		webView.sendJavascript("Weemo.internal.videoInChanged(" + e.getCall().getCallId() + ", " + (e.isReceivingVideo() ? "true" : "false") + ");");
	}*/


	public void endCallJS(){
		clearLocalSession();
		webView.sendJavascript("videoPlugin.internal.callStatusChanged('0', \'ENDED\');");
	}
	
	
	/*------------- Callbacks for QBRTC -----------------*/
		/**
	 * Called in case when connection establishment process is started
	 */
	 @Override
	public void onStartConnectToUser(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when connection with the opponent is established
	 */
	 @Override
	public void onConnectedToUser(QBRTCSession session, Integer userID){
	
		Log.d("AminLog","Connected to user in native plugin file");
		if(currentSession == session)
		webView.sendJavascript("videoPlugin.internal.callCreated(0, \'ACTIVE\');");
		
	}
 
	/**
	 * Called in case when connection is closed
	 */
	 @Override
	public void onConnectionClosedForUser(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when the opponent is disconnected
	 */

	 @Override
	public void onDisconnectedFromUser(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when the opponent is disconnected by timeout
	 */
	 @Override
	public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when connection has failed with the opponent
	 */
	 @Override
	public void onConnectionFailedWithUser(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case of some errors occurred during connection establishment process
	 */
	 @Override
	public void onError(QBRTCSession session, QBRTCException exception){}
	
	
		/**
	 * Called each time when new session request is received.
	 */
	 @Override
	public void onReceiveNewSession(QBRTCSession session)
	{
		Log.d("AminLog","Received new session in native plugin file. Someone is calling");
		currentSession = session;
		currentSession.addSessionCallbacksListener(this);
		//Tell JS the session is ringing
		webView.sendJavascript("videoPlugin.internal.callCreated(0, \'RINGING\');");
			//[[self commandDelegate]evalJs:[NSString stringWithFormat:@"videoPlugin.internal.callCreated(%d, \"%@\");", 0, @"RINGING"]];
	
	}
 
	/**
	 * Called in case when user didn't answer in timer expiration period
	 */
	 @Override
	public void onUserNotAnswer(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when opponent has rejected you call
	 */
	 @Override
	public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo){}
 
	/**
	 * Called in case when opponent has accepted you call
	 */
	 @Override
	public void onCallAcceptByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo){

         currentSession = session;



         //Display the call window once the user accepts
//         displayCallWindow(createCallCallback, 0, false);

     }
 
	/**
	 * Called in case when opponent hung up
	 */
	 @Override
	 public void onReceiveHangUpFromUser(QBRTCSession session, Integer userID, Map<String,String> userInfo){}
 
	/**
	 * Called in case when user didn't make any actions on received session
	 */
	 @Override
	public void onUserNoActions(QBRTCSession session, Integer userID){}
 
	/**
	 * Called in case when session will close
	 */
	@Override
	public void onSessionStartClose(QBRTCSession session){
	}
 
	/**
	 * Called when session is closed.
	 */
	 @Override
	public void onSessionClosed(QBRTCSession session){

         Log.d("AminLog","I am in the plugin and saw that the session closed");
         endCallJS();
     }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {

        Log.d("AminLog","I have received local track inside plugin");
        hasLocalTrack = true;
        currentLocalTrack = videoTrack;
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {

        Log.d("AminLog","I have received remote track inside plugin");
        hasRemoteTrack = true;
        currentRemoteTrack = videoTrack;
    }

}
