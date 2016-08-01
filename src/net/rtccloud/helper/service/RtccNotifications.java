package net.rtccloud.helper.service;

import java.util.ArrayList;

import com.synsormed.mobile.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import net.rtccloud.helper.MainActivity;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Contact;

/**
 * Helper class to display {@link Notification} in the Android Status Bar.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class RtccNotifications {

	/** TAG to identify the RingingCall Notification */
	public static final String NOTIF_RINGING_CALL_TAG = "NOTIF_RINGING_CALL_TAG";
	/** ID of the RingingCall Notification */
	public static final int NOTIF_RINGING_CALL_ID = 0;
	/** TAG to identify the Ongoing call Notification */
	public static final String NOTIF_ONGOING_CALL_TAG = "NOTIF_ONGOING_CALL_TAG";
	/** ID of the OngoingCall Notification */
	public static final int NOTIF_ONGOING_CALL_ID = 1;
	/** Vibrate pattern */
	private static final long[] VIBRATE = new long[] { 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000 };

	/** Pending Notification builder used to update the ongoing notification */
	public static NotificationCompat.Builder sOnGoingBuilder;

	/**
	 * Post a notification of a ringing call to be shown in the status bar.
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @param call
	 *            The ringing {@link Call}.
	 */
	public static void notifyRingingCall(final Context context, final Call call) {
		final Resources res = context.getResources();
		final Spanned ticker = buildRingingTitle(res, call);
		final ArrayList<Contact> contacts = call.getContacts();
		final String title = contacts == null || contacts.isEmpty() ? null : contacts.get(0).getDisplayName();
		final String text = res.getString(R.string.msg_calling_in_text);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setDefaults(Notification.DEFAULT_LIGHTS)
				.setVibrate(VIBRATE)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioManager.STREAM_RING)
				.setSmallIcon(R.drawable.ic_notif_ringing)
				.setContentTitle(title)
				.setContentText(text)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setTicker(ticker)
				/* Click on the notification will display the popup in the MainActivty */
				.setContentIntent(PendingIntent.getActivity(context, RtccService.Action.RINGING.ordinal(), new Intent(context, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.RINGING.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT))
				/* Click on the video button will accept the call with video */
				.addAction(R.drawable.ic_notif_action_video, res.getString(R.string.notif_ringing_accept_video),
						PendingIntent.getActivity(context, RtccService.Action.ACCEPT_CALL_VIDEO.ordinal(), new Intent(context, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.ACCEPT_CALL_VIDEO.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT)) //
				/* Click on the audio button will accept the call without video */
				.addAction(R.drawable.ic_notif_action_audio, res.getString(R.string.notif_ringing_accept_audio),
						PendingIntent.getActivity(context, RtccService.Action.ACCEPT_CALL_AUDIO.ordinal(), new Intent(context, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.ACCEPT_CALL_AUDIO.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT)) //
				/* Click on the reject button will reject the call */
				.addAction(R.drawable.ic_notif_action_reject, res.getString(R.string.notif_ringing_reject),
						PendingIntent.getActivity(context, RtccService.Action.REJECT_CALL.ordinal(), new Intent(context, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.REJECT_CALL.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT)) //
				.setAutoCancel(false);

		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIF_RINGING_CALL_TAG, NOTIF_RINGING_CALL_ID, builder.build());
	}

	/**
	 * Post a notification of an ongoing call to be shown in the status bar.
	 * 
	 * @param service
	 *            The {@link Service} associated with the notification. This service will be started in Foreground.
	 * @param call
	 *            The ongoing {@link Call}.
	 */
	public static void notifyOngoingCall(final Service service, final Call call) {
		final Resources res = service.getResources();
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(service)
				.setSmallIcon(R.drawable.ic_notif_ongoing_call)
				.setContentTitle(buildOnGoingTitle(res, call))
				.setContentText(res.getString(call.isConference() ? R.string.notif_ongoing_conference_tap : R.string.notif_ongoing_call_tap))
				.setPriority(NotificationCompat.PRIORITY_MAX)
				/* Click on the notification will resume the call */
				.setContentIntent(PendingIntent.getActivity(service, RtccService.Action.ONGOING_CALL.ordinal(), new Intent(service, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.ONGOING_CALL.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT))
				/* Click on the reject button will reject the call */
				.addAction(R.drawable.ic_notif_action_reject, res.getString(R.string.notif_call_hangup),
						PendingIntent.getActivity(service, RtccService.Action.HANGUP_CALL.ordinal(), new Intent(service, MainActivity.class).putExtra(RtccService.EXTRA_ACTION, RtccService.Action.HANGUP_CALL.ordinal()).putExtra(RtccService.EXTRA_CALLID, call.getCallId())//
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT)) //
				.setUsesChronometer(true).setOngoing(true).setOnlyAlertOnce(true).setAutoCancel(false);

		sOnGoingBuilder = builder;
		service.startForeground(NOTIF_ONGOING_CALL_ID, sOnGoingBuilder.build());
	}

	/**
	 * Cancel a previously shown ringing call {@link Notification}.
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 */
	public static void cancelRingingCall(final Context context) {
		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIF_RINGING_CALL_TAG, NOTIF_RINGING_CALL_ID);
	}

	/**
	 * Cancel a previously shown ongoing call {@link Notification} and remove the provided Service from the Foreground state.
	 * 
	 * @param service
	 *            The {@link Service} associated to the ongoing {@link Notification}.
	 */
	public static void cancelOngoingCall(final Service service) {
		sOnGoingBuilder = null;
		service.stopForeground(true);
		// cancel(service, NOTIF_ONGOING_TAG, NOTIF_ONGOING_ID);
	}

	/**
	 * @param res
	 *            The resources.
	 * @param call
	 *            The call to use.
	 * @return The ringing title.
	 */
	private static Spanned buildRingingTitle(Resources res, Call call) {
		if (call == null) {
			return null;
		}
		ArrayList<Contact> contacts = call.getContacts();
		return contacts == null || contacts.isEmpty() ? null : Html.fromHtml(res.getString(R.string.notif_ringing, (contacts.get(0).getDisplayName())));
	}

	/**
	 * @param res
	 *            The resources.
	 * @param call
	 *            The call to use.
	 * @return The ongoing title.
	 */
	private static Spanned buildOnGoingTitle(Resources res, Call call) {
		if (call == null) {
			return null;
		}
		StringBuilder sb;
		ArrayList<Contact> contacts = call.getContacts();
		if (contacts == null || contacts.isEmpty()) {
			sb = new StringBuilder(call.isConference() ? "Empty conference" : "Empty Call");
		} else {
			sb = new StringBuilder(call.isConference() ? "Conference with " : "Call with ");
			for (int i = 0; i < contacts.size(); i++) {
				sb.append(contacts.get(i).getDisplayName());
				if (i < contacts.size() - 1) {
					sb.append(", ");
				}
			}
		}

		return Html.fromHtml(res.getString(R.string.notif_ongoing_call, sb.toString()));
	}

	/**
	 * Update the ongoing {@link Notification} title.
	 * 
	 * @param service
	 *            The {@link Service} associated with the notification. This service will be started in Foreground if the notification didn't already exist.
	 * @param call
	 *            The ongoing {@link Call}.
	 */
	public static void updateOngoingCall(RtccService service, Call call) {
		if (sOnGoingBuilder == null) {
			notifyOngoingCall(service, call);
		} else {
			sOnGoingBuilder.setContentTitle(buildOnGoingTitle(service.getResources(), call));
			final NotificationManager nm = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIF_ONGOING_CALL_ID, sOnGoingBuilder.build());
		}
	}

}