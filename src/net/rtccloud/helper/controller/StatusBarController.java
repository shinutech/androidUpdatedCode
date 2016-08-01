package net.rtccloud.helper.controller;

//import net.rtccloud.helper.R;
import com.synsormed.mobile.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;


/**
 * Based on the <a href="http://code.google.com/p/romannurik-code/source/browse/misc/undobar" >UndoBar</a> by Roman Nurik.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 * 
 */
@SuppressWarnings("javadoc")
public class StatusBarController {
	private static final int HIDE_DELAY = 6000;
	View mBarView;
	private TextView mMessageView;
	private TextView mButtonView;
	private ViewPropertyAnimator mBarAnimator;
	private Handler mHideHandler = new Handler();

	StatusBarListener mStatusBarListener;

	// State objects
	Bundle mStatusBarToken;
	CharSequence mStatusBarMessage;
	CharSequence mStatusBarButton;
	StatusBarAction mStatusBarAction;

	public interface StatusBarListener {
		void onStatusBarClick(Bundle bundle, StatusBarAction action);
	}

	public enum StatusBarAction {
		CALL(R.drawable.ic_statusbar_call, "Call"), RETRY(R.drawable.ic_statusbar_retry, "Retry"), REPLY(R.drawable.ic_statusbar_reply, "Reply"), MEETING_POINT(R.drawable.ic_statusbar_acquire, "Acquire");
		public final int icon;
		public final String text;

		private StatusBarAction(int icon, String text) {
			this.icon = icon;
			this.text = text;
		}
	}

	public StatusBarController(View statusBarView, StatusBarListener statusBarListener) {
		this.mBarView = statusBarView;
		this.mBarAnimator = this.mBarView.animate();
		this.mStatusBarListener = statusBarListener;

		this.mMessageView = (TextView) this.mBarView.findViewById(R.id.statusbar_message);
		this.mMessageView.setMovementMethod(new ScrollingMovementMethod());
		this.mButtonView = (Button) this.mBarView.findViewById(R.id.statusbar_button);
		this.mButtonView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideStatusBar(false);
				StatusBarController.this.mStatusBarListener.onStatusBarClick(StatusBarController.this.mStatusBarToken, StatusBarController.this.mStatusBarAction);
			}
		});
		this.mMessageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideStatusBar(false);
			}
		});

		hideStatusBar(true);
	}

	public void showStatusBar(CharSequence message, StatusBarAction action, Bundle statusToken) {
		this.mStatusBarToken = statusToken;
		this.mStatusBarAction = action;
		this.mStatusBarMessage = message;
		this.mStatusBarButton = action.text;
		this.mMessageView.setText(Html.fromHtml(this.mStatusBarMessage.toString()));
		this.mButtonView.setText(this.mStatusBarButton);
		this.mButtonView.setCompoundDrawablesWithIntrinsicBounds(this.mStatusBarAction.icon, 0, 0, 0);

		this.mHideHandler.removeCallbacks(this.mHideRunnable);
		this.mHideHandler.postDelayed(this.mHideRunnable, HIDE_DELAY);
		this.mBarView.setVisibility(View.VISIBLE);

		this.mBarAnimator.cancel();
		this.mBarAnimator.alpha(1).setDuration(this.mBarView.getResources().getInteger(android.R.integer.config_longAnimTime)).setInterpolator(new DecelerateInterpolator(4)).setListener(null);
	}

	public void hideStatusBar(boolean immediate) {
		this.mHideHandler.removeCallbacks(this.mHideRunnable);
		if (immediate) {
			this.mBarView.setVisibility(View.GONE);
			this.mBarView.setAlpha(0);
			this.mStatusBarMessage = null;
			this.mStatusBarButton = null;
			this.mStatusBarToken = null;

		} else {
			this.mBarAnimator.cancel();
			this.mBarAnimator.alpha(0).setDuration(this.mBarView.getResources().getInteger(android.R.integer.config_mediumAnimTime)).setInterpolator(new AccelerateInterpolator(4)).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					StatusBarController.this.mBarView.setVisibility(View.GONE);
					StatusBarController.this.mStatusBarMessage = null;
					StatusBarController.this.mStatusBarButton = null;
					StatusBarController.this.mStatusBarToken = null;
				}
			});
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence("status_message", this.mStatusBarMessage);
		outState.putCharSequence("status_button", this.mStatusBarButton);
		outState.putParcelable("status_token", this.mStatusBarToken);
		if (this.mStatusBarAction != null) {
			outState.putInt("status_action", this.mStatusBarAction.ordinal());
		}
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			this.mStatusBarMessage = savedInstanceState.getCharSequence("status_message");
			this.mStatusBarButton = savedInstanceState.getCharSequence("status_button");
			this.mStatusBarToken = savedInstanceState.getParcelable("status_token");
			this.mStatusBarAction = StatusBarAction.values()[savedInstanceState.getInt("status_action")];

			if (this.mStatusBarToken != null || !TextUtils.isEmpty(this.mStatusBarMessage) || !TextUtils.isEmpty(this.mStatusBarButton)) {
				showStatusBar(this.mStatusBarMessage, this.mStatusBarAction, this.mStatusBarToken);
			}
		}
	}

	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hideStatusBar(false);
		}
	};
}