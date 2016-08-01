package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent.Error;

/**
 * A {@link DialogFragment} that is used to inform the user of an unsuccessful {@link AuthenticatedEvent} with error {@link Error#NETWORK_LOST} during a {@link Call}.<br />
 * It happens when the device lost network connection.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class ReconnectingDialogFragment extends DialogFragment {

	/** Key to persist the call id */
	private static final String KEY_CALL_ID = "KEY_CALL_ID";

	/** The {@link Call} associated to this {@link ReconnectingDialogFragment} */
	Call mCall;

	/**
	 * Factory method. Creates a new instance of this fragment using the provided {@link Call}.
	 * 
	 * @param call
	 *            The {@link Call} to associate.
	 * @return A new instance of {@link ReconnectingDialogFragment}.
	 */
	public static ReconnectingDialogFragment newInstance(Call call) {
		ReconnectingDialogFragment frag = new ReconnectingDialogFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_CALL_ID, call.getCallId());
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);
		if (this.mCall == null || this.mCall.getStatus() != CallStatus.ACTIVE) {
			dismiss();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		/* Unregister the fragment */
		Rtcc.eventBus().unregister(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		this.mCall = Rtcc.instance().getCall(getArguments().getInt(KEY_CALL_ID));
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setMessage(getString(R.string.msg_reconnecting));
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.msg_reconnecting_cancel), new OnClickListener() {
			@Override
			public void onClick(final DialogInterface paramDialogInterface, final int paramInt) {
				dismiss();
				ReconnectingDialogFragment.this.mCall.hangup();
			}
		});
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
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
		switch (event.getStatus()) {
		case ENDED:
			dismiss();
			break;
		default:
			break;
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
	public void onAuthenticatedEvent(final AuthenticatedEvent event) {
		if (event.isSuccess()) {
			dismiss();
		}
	}

}
