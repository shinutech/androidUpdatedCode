package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Html;
import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;

/**
 * A {@link DialogFragment} that is used to inform the user of a {@link Call} in {@link CallStatus#PROCEEDING} state. It offers a button to cancel the call before it becomes
 * {@link CallStatus#ACTIVE} or {@link CallStatus#ENDED}.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class CallingDialogFragment extends DialogFragment {

	/** Key to persist the call id */
	private static final String KEY_CALL_ID = "KEY_CALL_ID";

	/** The {@link Call} associated to this {@link CallingDialogFragment} */
	protected Call mCall;

	/**
	 * Factory method. Creates a new instance of this fragment using the provided {@link Call}.
	 * 
	 * @param call
	 *            The {@link Call} to associate.
	 * @return A new instance of {@link CallingDialogFragment}.
	 */
	public static CallingDialogFragment newInstance(Call call) {
		CallingDialogFragment frag = new CallingDialogFragment();
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
		if (this.mCall == null || this.mCall.getStatus() != CallStatus.PROCEEDING) {
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
		dialog.setMessage(Html.fromHtml(getString(R.string.msg_calling_out, this.mCall == null ? "" : this.mCall.getContactDisplayName(Contact.DEFAULT_CONTACT_ID))));
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.msg_calling_hang_up), new OnClickListener() {
			@Override
			public void onClick(final DialogInterface paramDialogInterface, final int paramInt) {
				dismiss();
				if (CallingDialogFragment.this.mCall != null) {
					App.breadcrumb("Call.hangup()");
					CallingDialogFragment.this.mCall.hangup();
				}
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
		case ACTIVE:
		case ENDED:
			dismiss();
			break;
		default:
			break;
		}
	}
}
