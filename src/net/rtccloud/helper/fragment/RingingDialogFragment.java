package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Call.StartingOptions;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;

/**
 * A {@link DialogFragment} that is used to inform the user of a {@link Call} in {@link CallStatus#RINGING} state. It offers a button to accept the call and one to reject the call, before it
 * becomes {@link CallStatus#ACTIVE} or {@link CallStatus#ENDED}.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class RingingDialogFragment extends DialogFragment {

	/** Tag used to identify the {@link RingingDialogFragment} */
	public static final String TAG = RingingDialogFragment.class.getSimpleName();
	/** Key to persist the call id */
	private static final String KEY_CALL_ID = "KEY_CALL_ID";

	/** The {@link Call} associated to this {@link RingingDialogFragment} */
	Call mCall;

	/**
	 * Factory method. Creates a new instance of this fragment using the provided {@link Call}.
	 * 
	 * @param call
	 *            The {@link Call} to associate.
	 * @return A new instance of {@link RingingDialogFragment}.
	 */
	public static RingingDialogFragment newInstance(Call call) {
		RingingDialogFragment frag = new RingingDialogFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_CALL_ID, call.getCallId());
		frag.setArguments(args);
		return frag;
	}

	/**
	 * Helper method to show a {@link RingingDialogFragment} with the provided {@link Call}.
	 * 
	 * @param call
	 *            The {@link Call} to associate.
	 * @param fm
	 *            The {@link FragmentManager} to use.
	 */
	public static void show(Call call, FragmentManager fm) {
		if (call == null) {
			Log.w(TAG, "Cann't show the " + TAG + " with a null call");
			return;
		}
		RingingDialogFragment fragment = (RingingDialogFragment) fm.findFragmentByTag(TAG);
		if (fragment == null) {
			RingingDialogFragment.newInstance(call).show(fm, TAG);
		}
	}

	/**
	 * Helper method to hide a {@link RingingDialogFragment}.
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use.
	 */
	public static void hide(FragmentManager fm) {
		RingingDialogFragment fragment = (RingingDialogFragment) fm.findFragmentByTag(TAG);
		if (fragment != null) {
			fragment.dismiss();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);
		if (this.mCall == null || this.mCall.getStatus() != CallStatus.RINGING) {
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
		AlertDialog d = new AlertDialog.Builder(getActivity())//
				.setIcon(R.drawable.ic_dialog_ringing)//
				.setTitle(Html.fromHtml(getString(R.string.msg_calling_in, this.mCall.getContactDisplayName(Contact.DEFAULT_CONTACT_ID))))//
				.setPositiveButton(R.string.msg_calling_accept_video, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
						App.breadcrumb("Call.resume()");
						RingingDialogFragment.this.mCall.resume();
					}
				})//
				.setNeutralButton(R.string.msg_calling_accept_audio, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
						StartingOptions options = new StartingOptions.Builder().setVideoEnabled(false).build();
						App.breadcrumb("Call.resume(%s)", options.toString());
						RingingDialogFragment.this.mCall.resume(options);
					}
				})//
				.setNegativeButton(R.string.msg_calling_reject, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
						App.breadcrumb("Call.hangup()");
						RingingDialogFragment.this.mCall.hangup();
					}
				})//
				.create();
		d.setCanceledOnTouchOutside(false);
		setCancelable(false);
		return d;
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
