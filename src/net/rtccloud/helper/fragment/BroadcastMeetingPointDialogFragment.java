package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.MeetingPoint;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class BroadcastMeetingPointDialogFragment extends DialogFragment {
	
	/** Start tag used to detect MeetingPoint identifier */
	public static final String MEETINGPOINT_START_TAG = "*#*#";
	/** End tag used to detect MeetingPoint identifier */
	public static final String MEETINGPOINT_END_TAG = "#*#*";
	/** Argument used to provide a MeetingPoint id */
	private static final String ARG_MP_ID = "ARG_MP_ID";

	/**
	 * Factory method. Creates a new instance of this fragment using the provided {@link MeetingPoint}.
	 * 
	 * @param mp
	 *            The {@link MeetingPoint} to associate.
	 * @return A new instance of {@link BroadcastMeetingPointDialogFragment}.
	 */
	public static BroadcastMeetingPointDialogFragment newInstance(MeetingPoint mp) {
		BroadcastMeetingPointDialogFragment frag = new BroadcastMeetingPointDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_MP_ID, mp.getId());
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		/* Unregister the fragment */
		Rtcc.eventBus().unregister(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_invite_meetingpoint, null);
		final EditText contactId = (EditText) view.findViewById(R.id.mp_broadcast);

		AlertDialog.Builder builder = new Builder(getActivity()).setTitle(R.string.action_broadcast_mp).setView(view).setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				App.breadcrumb("broadcastMeetingPoint(%s) {%s}", contactId.getText().toString(), getArguments().getString(ARG_MP_ID));
				Rtcc.instance().sendDataToContact((MEETINGPOINT_START_TAG + getArguments().getString(ARG_MP_ID) + MEETINGPOINT_END_TAG).getBytes(), contactId.getText().toString());
				dismiss();
			}
		});
		return builder.create();
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
	public void onAuthenticatedEvent(final AuthenticatedEvent event) {
		if (!event.isSuccess()) {
			dismiss();
		}
	}

}
