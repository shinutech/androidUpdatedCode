package com.weemo.phonegap;

import javax.annotation.CheckForNull;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * This is a very simple fragment that creates a loading Dialogfragment.
 * It is used  multiple times in this project
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class LoadingDialogFragment extends DialogFragment {

	/** Fragment required string argument key: the title of the dialog */
	private static final String ARG_TITLE = "title";

	/** Fragment required string argument key: the text of the dialog */
	private static final String ARG_TEXT = "text";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param title The title of the dialog
	 * @param text The text of the dialog
	 * @param cancel Text for cancel button (can be null)
	 * @return The created fragment
	 */
	public static LoadingDialogFragment newFragmentInstance(final String title, final String text, final @CheckForNull String cancel) {
		final LoadingDialogFragment fragment = new LoadingDialogFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_TEXT, text);
		if (cancel != null) {
			args.putString("cancel", cancel);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(getActivity());

		dialog.setTitle(getArguments().getString(ARG_TITLE));
		dialog.setMessage(getArguments().getString(ARG_TEXT));

		String cancel = getArguments().getString("cancel");
		if (cancel != null) {
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, new OnClickListener() {
				@Override public void onClick(final DialogInterface dlg, int which) {
					Activity activity = getActivity();
					if (activity instanceof DialogInterface.OnCancelListener) {
						((DialogInterface.OnCancelListener) activity).onCancel(dialog);
					}
				}
			});
		}

		return dialog;
	}


}
