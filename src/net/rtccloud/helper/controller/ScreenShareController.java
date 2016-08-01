package net.rtccloud.helper.controller;

import com.synsormed.mobile.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

//import net.rtccloud.helper.R;
import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.util.Ui;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.VideoProfile;
import net.rtccloud.sdk.Contact;

/**
 * Helper class to control and manage the Screen Share inside the {@link CallFragment}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class ScreenShareController {

	/** The call */
	private Call call;

	/** The View used for screen sharing */
	protected View mShareView;
	/** The WebView inside the {@link #mShareView} */
	protected WebView mWebView;
	/** The EditText inside the {@link #mShareView} */
	protected EditText mUrlBar;
	/** The URL value */
	protected String mUrl;

	/** If the screen share was paused (while going to background) */
	private boolean paused;

	/**
	 * Initialise the {@link ScreenShareController}
	 * 
	 * @param shareView
	 *            The root of the view to share
	 * @param savedInstanceState
	 *            A {@link Bundle} used to store data
	 * @param c
	 *            The corresponding {@link Call}
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void init(View shareView, Bundle savedInstanceState, Call c) {
		this.call = c;
		this.mShareView = shareView;
		this.mWebView = (WebView) this.mShareView.findViewById(R.id.share_webview);
		this.mUrlBar = (EditText) this.mShareView.findViewById(R.id.share_bar);
		if (savedInstanceState != null) {
			this.mWebView.restoreState(savedInstanceState);
		}
		this.mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				screenShareSendNewUrl(url);
				return true;
			}
		});
		this.mUrlBar.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					screenShareSendNewUrl(ScreenShareController.this.mUrlBar.getText().toString());
				}
				return true;
			}
		});
		WebSettings settings = this.mWebView.getSettings();
		settings.setSupportZoom(true);
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		this.mUrlBar.setText(this.mUrl);

		if (this.call.isSendingScreenShare()) {
			this.call.screenShareStart(this.mShareView);
		}
	}

	/**
	 * Start the Screen Sharing.<br />
	 * This will perform a request to the Rtcc SDK to start Screen Sharing.
	 */
	public void start() {
		this.paused = false;
		this.call.screenShareStart(this.mShareView);
	}

	/**
	 * Make the Screen Sharing visible.<br />
	 * {@link #start()} must be called before.
	 */
	public void postStart() {
		if (this.call != null) {
			this.mShareView.setVisibility(View.VISIBLE);
			if (!this.paused) {
				screenShareSendNewUrl(this.mUrl);
			}
			if (!this.call.isConference() && this.call.getVideoInProfile(Contact.DEFAULT_CONTACT_ID) == VideoProfile.HD) {
				this.call.setInVideoProfile(VideoProfile.SD);
			}
		}
		this.paused = false;
	}

	/**
	 * Flag the {@link ScreenShareController} as paused.<br />
	 * Should be used to handle configuration changes.
	 */
	public void pause() {
		this.paused = true;
	}

	/**
	 * @return <code>true</code> if the {@link ScreenShareController} was previously paused, <code>false</code> otherwise
	 */
	public boolean isPaused() {
		return this.paused;
	}

	/**
	 * Stop the Screen Sharing.<br />
	 * This will perform a request to the SDK to stop Screen Sharing.
	 */
	public void stop() {
		if (this.call != null) {
			this.call.screenShareStop();
		}
	}

	/**
	 * Hide the Screen Sharing.<br />
	 * {@link #stop()} must be called before.
	 */
	public void postStop() {
		this.mShareView.setVisibility(View.GONE);
	}

	/**
	 * Load the provided URL into the {@link WebView}
	 * 
	 * @param url
	 *            The URL to load
	 */
	public void screenShareSendNewUrl(String url) {
		String sanitizedUrl = (url == null ? "http://www.sightcall.com/" : (url.contains("://") ? "" : "http://") + url);
		this.mUrl = sanitizedUrl;
		this.mUrlBar.setText(sanitizedUrl);
		this.mWebView.loadUrl(sanitizedUrl);
		Selection.removeSelection(this.mUrlBar.getEditableText());
		Ui.hideSoftKeyboard(this.mUrlBar.getContext(), this.mUrlBar);
	}

	/**
	 * Persist {@link WebView} instance to the provided {@link Bundle}
	 * 
	 * @param outState
	 *            The bundle to store data into
	 */
	public void saveState(Bundle outState) {
		this.mWebView.saveState(outState);
	}

}
