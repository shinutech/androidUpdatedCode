package net.rtccloud.helper.util;

import android.os.CountDownTimer;
import android.util.Log;

import net.rtccloud.helper.App;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;

/**
 * CountDown helper class. It contains two methods to start and cancel the countdown to send the {@link RtccEngine} to background mode by calling {@link RtccEngine#goToBackground()}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class BackgroundTimer {

	/** Tag used for logging purpose */
	static final String TAG = BackgroundTimer.class.getSimpleName();

	/** CountDown timer */
	private static CountDownTimer timer;

	/**
	 * Start the countdown. It won't be started if there is a call in progress or if the {@link RtccEngine} is already in background mode.
	 */
	public synchronized static void startCountDown() {
		Log.i(TAG, "startCountDown()");
		if (Rtcc.instance() == null || Rtcc.instance().getCurrentCall() != null || Rtcc.instance().isInBackground()) {
			Log.i(TAG, "Already in background mode or a call in progress, can't start the countdown");
			return;
		}
		if (BackgroundTimer.timer == null) {
			/* 10s countdown to go into background mode */
			BackgroundTimer.timer = new CountDownTimer(10000, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					Log.i(TAG, millisUntilFinished / 1000L + "s until going to background mode");
				}

				@Override
				public void onFinish() {
					final RtccEngine instance = Rtcc.instance();
					if (instance != null && instance.getCurrentCall() == null && !instance.isInBackground()) {
						/* If there is no call going on, then we go to background which allows the Rtcc engine to save battery */
						Log.i(TAG, "Going to background mode");
						App.breadcrumb("RtccEngine.goToBackground()");
						instance.goToBackground();
					}
				}
			};
		} else {
			BackgroundTimer.cancelCountDown();
		}
		Log.i(TAG, "Starting countDown to background mode");
		BackgroundTimer.timer.start();
	}

	/**
	 * Cancel the countdown.
	 */
	public synchronized static void cancelCountDown() {
		if (BackgroundTimer.timer != null) {
			BackgroundTimer.timer.cancel();
		}
	}
}
