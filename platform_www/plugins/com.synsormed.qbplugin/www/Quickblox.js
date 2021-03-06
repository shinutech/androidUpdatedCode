cordova.define("com.synsormed.qbplugin.videoPlugin", function(require, exports, module) {


	var exec = require("cordova/exec");

	/**
	 * @class Weemo
	 * Main Weemo interface
	 */
	var Weemo = {};
	var videoPlugin = {};
	
	
	
	videoPlugin.initVideo = function()
	{
		console.log("I am about to go to native");
		exec(null, null, "videoPlugin", "initQB", []);
	
		
	}

	var e_INIT	= 0x0100;
	var e_CONN	= 0x0200;
	var e_AUTH	= 0x0300;

	/**
	 * Weemo error codes
	 */
	Weemo.Error = {
		/**
		 * Could not connect
		 */
		NETWORK_ERROR:			e_INIT | 1,
		/**
		 * The provided API Key is not valid
		 */
		BAD_APIKEY:				e_INIT | 2,
		/**
		 * You are not connected
		 */
		NOT_CONNECTED:			e_INIT | 3,
		/**
		 * You are already authenticated
		 */
		ALREADY_AUTHENTICATED:	e_INIT | 4,

		/**
		 * You've temporary lost the network
		 */
		NETWORK_LOST:			e_CONN | 1,
		/**
		 * You've closed the connection
		 */
		CLOSED:					e_CONN | 2,

		/**
		 * There was an error while trying to authenticate
		 */
		SIP_NOK:				e_AUTH | 1
	};

	/**
	 * Call statuses
	 */
	Weemo.CallStatus = {
		/**
		 * The call has just been created
		 */
		CREATED:	0,
		/**
		 * The call should be ringing on this device
		 */
		RINGING:	0x7120,
		/**
		 * The call is active
		 */
		ACTIVE:		0x7130,
		/**
		 * The call is ended
		 */
		ENDED:		0x7140,
		/**
		 * The call is ringing on the contact's device
		 */
		PROCEEDING:	0x7150,
		/**
		 * The call is paused (Not implemented yet)
		 */
		PAUSED:		0x7160
	};

	/**
	 * User types
	 */
	Weemo.UserType = {
		INTERNAL: 0,
		EXTERNAL: 1
	}

	/**
	 * @class WeemoCall
	 * Class that represents an ongoing call
	 * Objects are created by the weemo plugin.
	 * New WeemoCalls are signaled to you by the onCallStatusChanged callback.
	 */
	var WeemoCall = function(callID, displayName) {
		/**
		 * @treturn Number ID of this call
		 */
		this.getCallID = function() { return callID; };

		/**
		 * @treturn String The remote contact display name
		 */
		this.getContactDisplayName = function() { return displayName; };

		callStatus = Weemo.CallStatus.CREATED;

		/**
		 * @treturn Number The call status. Is one of Weemo.CallStatus.*
		 */
		this.getCallStatus = function() { return callStatus; };

		this._setCallStatus = function(cs) { callStatus = cs; };

		receivingVideo = false;

		/**
		 * @treturn Boolean Whether or not the call is currently receiving video
		 */
		this.isReceivingVideo = function() { return receivingVideo; };

		this._setReceivingVideo = function(rv) { receivingVideo = rv; };

		/**
		 * Mute (or unmute) the microphone
		 * @param Boolean mute  Whether or not to mute the mircophone
		 */
		this.muteOut = function(mute) {
			exec(null, null, "weemo", "muteOut", [callID, mute]);
		};

		/**
		 * Pick up or resume a call
		 * @param Boolean isProvider , is provider is resuming
		 */
		this.resume = function(isProvider) {
			exec(null, null, "weemo", "resume", [callID,isProvider]);
		};

		/**
		 * Hang up or refuse a call
		 */
		this.hangup = function() {
			exec(null, null, "weemo", "hangup", [callID]);
		};

		/**
		 * Display a call window
		 * @param Boolean canComeBack Whether or not user can come back from call window (if not, a hangup button will be shown)
		 * @param Function onBack Called when the user comes back from the call window
		 */
		this.displayCallWindow = function(canComeBack, onBack) {
			exec(onBack, null, "weemo", "displayCallWindow", [callID, canComeBack]);
		};

		/**
		 * Display a call view above the webview
		 */
		this.displayCallView = function() {
			exec(null, null, "weemo", "displayCallView", [callID]);
		};

		/**
		 * Hide the call view above if it was displayed
		 */
		this.hideCallView = function() {
			exec(null, null, "weemo", "hideCallView", []);
		};

		/**
		 * Callback to be overriden: called when the status of this call changes
		 * @param int status New status of the call (refer to Weemo.CallStatus.*)
		 */
		this.onCallStatusChanged = function(status){};

		/**
		 * Callback to be overriden: called when the incoming video starts (or stops)
		 * @param Boolean receiving Whether or not this call is now receiving video
		 */
		this.onVideoInChanged = function(receiving){};
	};

	var calls = {};


	/**
	 * Callback to be overriden: called when the connection changes
	 * @param Number status: - 0 if the connection is back up<br />
	 *                       - Weemo.Error.NETWORK_LOST if the network is temporary lost<br />
	 *                       - Weemo.Error.CLOSED if the connection has been properly closed<br />
	 */
	Weemo.onConnectionChanged = function(status){};

	/**
	 * Callback to be overriden: called when a call has been created.
	 * At this point, you don't know anything about the call.
	 * You must, however, to set onCallStatusChanged callback.
	 * You can (but not must) set the onVideoInChanged callback.
	 * @param WeemoCall call The call that has just been created
	 */
	Weemo.onCallCreated = function(call){};

	/**
	 * Callback to be overriden IF you want to be called when the audio route changed
	 * @param Boolean speakers Whether or not the audio is going through external speakers.
	 */
	Weemo.onAudioRouteChanged = function(speakers){};

	/**
	 * Initializes weemo
	 * @param String appID Your Weemo appID
	 * @param Function success Callback called when connection has succeeded
	 * @param Function error Callback called when connection has failed (With a Weemo.Error.*)
	 */
	videoPlugin.initialize = function(appID, success, error) {
		exec(success, error, "videoPlugin", "initQB", [appID]);
	};

	Weemo.getEngineStatus = function() {
    		exec(this.engineGood, this.engineBad, "weemo", "getEngineStatus", []);
    	};

    Weemo.engineGood = function(status){

        console.log("The engine status is: " + status);

    }

    Weemo.engineBad = function(){
        console.log("There was an error getting Engine Status");
    }

	/**
	 * Authenticates weemo
	 * @param String token The token (userID) that identifies the current user
	 * @param Number type Must be Weemo.UserType.INTERNAL or Weemo.UserType.EXTERNAL
	 * @param Function success Callback called when connection has succeeded
	 * @param Function error Callback called when connection has failed (Called with a Weemo.Error.*)
	 */
	videoPlugin.authenticate = function(token, type, success, error) {
		exec(success, error, "videoPlugin", "authent", [token, type]);
	};

	/**
	 * Set the display name of the currently authenticated user
	 * @param String displayName The displayName to be set
	 */
	Weemo.setDisplayName = function(displayName) {
		exec(null, null, "weemo", "setDisplayName", [displayName]);
	};

	/**
	 * Ask for the status (whether the given uid is callable)
	 * @param String userID The userID of the user to check
	 * @param Function on Callback called when the answer is ready (Called with a Boolean)
	 */
	videoPlugin.getStatus = function(userID, on) {
		exec(function(available) { available ? on(true) : on(false); }, null, "videoPlugin", "getStatus", [userID]);
	};

	/**
	 * Create a call
	 * @param String userID The user to call
	 * @param Boolean isProvider , is provider accessing the call view
	 * @param Boolean allowSwitch , allow switching to web view
	 */
	videoPlugin.createCall = function(userID,isProvider,allowSwitch) {
		exec(null, null, "videoPlugin", "createCall", [userID,isProvider,allowSwitch]);
	};

	/**
	 * Disconnects from Weemo.
	 * Weemo.onConnectionChanged will then be called with Weemo.Error.CLOSED
	 */
	Weemo.disconnect = function() {
		exec(null, null, "weemo", "disconnect", []);
	};

	/**
	 * Ask the device to change IN audio route
	 * @param Boolean speakers Whether or not the audio will go through external speakers
	 */
	Weemo.setAudioRoute = function(speakers) {
		exec(null, null, "weemo", "setAudioRoute", [speakers]);
	};
	
	/**
	* Get LTE status from plugin.
	* Returns either strings LTE or OTHER all capitalized
	*/
   Weemo.getNetworkType = function(success, error) {
			   exec(success, error, "weemo", "getNetworkType", []);
   };
   
   
   videoPlugin.acceptCall = function(){
   
   		exec(null,null,"videoPlugin","acceptCall",[]);
   
   };
   
   videoPlugin.hangUp = function(){
   
   		exec(null,null,"videoPlugin","hangUp",[]);
   
   };

	videoPlugin.internal = {
		callCreated: function(callID, status) {
			//var call = new WeemoCall(callID, displayName);
			//calls[callID] = call;
			//Weemo.onCallCreated(call);
			console.log("Incoming call: JS, status is: " + status);
			angular.element(document.querySelector('#pageContainer')).scope().$broadcast('callchange',status);
		},

		callStatusChanged: function(callID, status) {
            console.log("Call status just changed in JS to: " + status);
               //console.log("The callID in js is: " + callID);
			/*var call = calls[callID];
               if (!call) {
               console.log("Call does not exist in js. Returning");
               return ;
               }
               if (status == 0) {
               console.log("Status is 0 in js. Returning");
               return ;
               }
			call._setCallStatus(status);
			call.onCallStatusChanged(status);
			*/
			angular.element(document.querySelector('#pageContainer')).scope().$broadcast('callchange',status);

		},

		videoInChanged: function(callID, receiving) {
			var call = calls[callID];
			if (!call) return ;
			call._setReceivingVideo(receiving);
			call.onVideoInChanged(receiving);
		},

		connectionChanged: function(status) {
			Weemo.onConnectionChanged(status);
		},

		audioRouteChanged: function(speakers) {
			Weemo.onAudioRouteChanged(speakers);
		}
	};

	module.exports = videoPlugin;



});
