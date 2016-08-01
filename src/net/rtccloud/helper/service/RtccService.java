package net.rtccloud.helper.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;

import net.rtccloud.helper.MainActivity;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.ParticipantListEvent;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.global.DataChannelEvent;

/**
 * This {@link Service} is always ON. It is responsible for dispatching incomming calls (and playing the ringtone) when the application is not in foreground. It displays relevant {@link Notification}s
 * through {@link RtccNotifications}.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class RtccService extends Service {

	/** Key corresponding to the action */
	public static final String EXTRA_ACTION = "EXTRA_ACTION";
	/** Key corresponding to the callID */
	public static final String EXTRA_CALLID = "EXTRA_CALLID";
	/** Key corresponding to the message sender displayName */
	public static final String EXTRA_DISPLAY_NAME = "EXTRA_DISPLAY_NAME";
	/** Key corresponding to the message sender ID */
	public static final String EXTRA_SENDER_ID = "EXTRA_SENDER_ID";
	/** Key corresponding to the message payload */
	public static final String EXTRA_PAYLOAD = "EXTRA_PAYLOAD";
	/** Key corresponding to the meeting point id */
	public static final String EXTRA_MEETING_POINT_ID = "EXTRA_MEETING_POINT_ID";

	/**
	 * Enum of the actions that {@link RtccService} can trigger.
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public static enum Action {
		/** UNKNOWN */
		UNKNOWN,
		/** RINGING */
		RINGING,
		/** ACCEPT_CALL_VIDEO */
		ACCEPT_CALL_VIDEO,
		/** ACCEPT_CALL_AUDIO */
		ACCEPT_CALL_AUDIO,
		/** REJECT_CALL */
		REJECT_CALL,
		/** RESUME_CALL */
		RESUME_CALL,
		/** HANGUP_CALL */
		HANGUP_CALL,
		/** ONGOING_CALL */
		ONGOING_CALL,
		/** MESSAGE */
		MESSAGE;

		/**
		 * @param index
		 *            The index to look for.
		 * @return The {@link Action} associated to the provided index.
		 */
		public static Action fromOrdinal(int index) {
			Action[] values = Action.values();
			if (index < 0 || index >= values.length) {
				return null;
			}
			return values[index];
		}
	}

	/** Ringtone used to notify incomming calls */
	protected Ringtone ringtone;
	/** Handler used to stop ringtone */
	private Handler stopRingtoneHandler;
	/** Runnable used to stop ringtone */
	private Runnable stopRingtoneRunnable = new Runnable() {
		@Override
		public void run() {
			if (RtccService.this.ringtone != null && RtccService.this.ringtone.isPlaying()) {
				RtccService.this.ringtone.stop();
			}
		}
	};

	/**
	 * Constructor
	 */
	public RtccService() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.stopRingtoneHandler = new Handler();
		/* Register the service as an event listener */
		Rtcc.eventBus().register(this);
	}

	/**
	 * Unregisters itself as a listener when destroyed
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		/* Unregister the service */
		Rtcc.eventBus().unregister(this);
	}

	/**
	 * This method catches all {@link StatusEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link StatusEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link StatusEvent}
	 */
	@RtccEventListener
	public void onCallStatusChanged(final StatusEvent event) {
		/* Stop the ringtone if its playing */
		this.stopRingtoneHandler.removeCallbacks(this.stopRingtoneRunnable);
		if (this.ringtone != null && this.ringtone.isPlaying()) {
			this.ringtone.stop();
		}

		switch (event.getStatus()) {
		case ENDED:
			RtccNotifications.cancelOngoingCall(this);
			RtccNotifications.cancelRingingCall(this);
			break;
		case RINGING:
			RtccNotifications.notifyRingingCall(getApplicationContext(), event.getCall());
			if (this.ringtone == null) {
				this.ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
			}
			if (this.ringtone != null) {
				this.ringtone.play();
				this.stopRingtoneHandler.postDelayed(this.stopRingtoneRunnable, 30000);
			}
			/* Start or resume the MainActivity and ask to display the ringing call */
			getApplicationContext().startActivity(new Intent(getApplicationContext(), MainActivity.class) //
					.putExtra(EXTRA_ACTION, Action.RINGING.ordinal())//
					.putExtra(EXTRA_CALLID, event.getCall().getCallId())//
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP));
			break;
		case ACTIVE:
			RtccNotifications.notifyOngoingCall(this, event.getCall());
			RtccNotifications.cancelRingingCall(this);
			break;
		case CREATED:
			break;
		case PAUSED:
			RtccNotifications.cancelOngoingCall(this);
			RtccNotifications.cancelRingingCall(this);
			break;
		case PROCEEDING:

		default:
			break;
		}
	}

	/**
	 * This method catches all {@link ParticipantListEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link ParticipantListEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link ParticipantListEvent}
	 */
	@RtccEventListener
	public void onCallConferenceParticipantListEvent(ParticipantListEvent event) {
		RtccNotifications.updateOngoingCall(this, event.getCall());
	}

	/**
	 * This method catches all {@link DataChannelEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link DataChannelEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link DataChannelEvent}
	 */
	@RtccEventListener
	public void onDataChannel(final DataChannelEvent event) {
		/* Start or resume the MainActivity and ask to display the ringing call */
		getApplicationContext().startActivity(new Intent(getApplicationContext(), MainActivity.class) //
				.putExtra(EXTRA_ACTION, Action.MESSAGE.ordinal())//
				.putExtra(EXTRA_DISPLAY_NAME, event.getDisplayName())//
				.putExtra(EXTRA_SENDER_ID, event.getId())//
				.putExtra(EXTRA_PAYLOAD, event.getPayload())//
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP));
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
