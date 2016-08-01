package net.rtccloud.helper.controller;

import android.app.Activity;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;

import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.listener.OnFullScreenListener;
import net.rtccloud.helper.view.CallControls;
import net.rtccloud.sdk.Call;

/**
 * Helper class to control and manage the Fullscreen mode inside the {@link CallFragment}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class FullScreenController implements OnSystemUiVisibilityChangeListener, OnTouchListener {

	/** The call */
	private Call call;
	/** The floating window controller */
	private FloatingWindowController floatingWindowController;
	/** The call controls */
	private CallControls controls;
	/** The root view used to capture touch events */
	private View root;

	/** Timeout of fullscreen request: 5s */
	private static final long FULLSCREEN_DELAY = 5000;
	/** Handler used for fullscreen request */
	protected Handler mFullScreenHandler = new Handler(Looper.getMainLooper());
	/** Runnable used to request fullscreen */
	protected Runnable mFullScreenRunnable = new Runnable() {
		@Override
		public void run() {
			onFullScreen(true);
		}
	};

	/** Callback used to dispatch the fullscreen request */
	private OnFullScreenListener mFullScreenListener;

	/**
	 * Sets the {@link Call}
	 * 
	 * @param call
	 */
	public void setCall(Call call) {
		this.call = call;
	}

	/**
	 * Sets the floating window controller
	 * 
	 * @param controller
	 */
	public void setFloatingWindowController(FloatingWindowController controller) {
		this.floatingWindowController = controller;
	}

	/**
	 * Sets the {@link CallControls} view
	 * 
	 * @param view
	 */
	public void setCallControls(CallControls view) {
		this.controls = view;
	}

	/**
	 * Sets the root view to capture touch events
	 * 
	 * @param view
	 */
	public void setRoot(View view) {
		if (this.root != null) {
			this.root.setOnTouchListener(null);
		}
		this.root = view;
		this.root.setOnTouchListener(this);
	}

	/**
	 * Sets the {@link OnFullScreenListener} callback.<br/>
	 * Call this method from the {@link Fragment#onAttach(Activity)} method.
	 * 
	 * @param listener
	 *            the callback
	 */
	public void onAttach(OnFullScreenListener listener) {
		this.mFullScreenListener = listener;
	}

	/**
	 * Call this method from the {@link Fragment#onStart()} method to schedule a fullscreen request
	 */
	public void onStart() {
		schedule();
	}

	/**
	 * Call this method from the {@link Fragment#onResume()} method to register a {@link OnSystemUiVisibilityChangeListener}
	 * 
	 * @param fragment
	 *            The corresponding fragment
	 */
	public void onResume(Fragment fragment) {
		fragment.getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
	}

	/**
	 * Call this method from the {@link Fragment#onStop()} method to cancel a fullscreen request
	 */
	public void onStop() {
		cancel();
	}

	/**
	 * Remove the {@link OnFullScreenListener} callback.<br/>
	 * Call this method from the {@link Fragment#onDetach()} method.
	 */
	public void onDetach() {
		if (this.mFullScreenListener != null) {
			this.mFullScreenListener.onFullScreen(false);
		}
		this.mFullScreenListener = null;
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
			schedule();
		}
	}

	/**
	 * Schedule the fullscreen request
	 */
	public void schedule() {
		cancel();
		this.mFullScreenHandler.postDelayed(this.mFullScreenRunnable, FULLSCREEN_DELAY);
	}

	/**
	 * Remove the pending fullscreen request and force display the system UI and the {@link CallControls}
	 */
	public void cancel() {
		this.mFullScreenHandler.removeCallbacks(this.mFullScreenRunnable);
		onFullScreen(false);
	}

	/**
	 * Dispatch the fullscreen request to the host {@link Activity} and hide the {@link CallControls}
	 * 
	 * @param enable
	 *            <code>true</code> to go fullscreen, <code>false</code> to restore the full UI
	 */
	protected void onFullScreen(boolean enable) {
		if (this.call == null) {
			return;
		}

		if (this.mFullScreenListener != null) {
			if (!this.call.isSendingScreenShare() && !this.floatingWindowController.isFloating()) {
				this.mFullScreenListener.onFullScreen(enable);
			}
		}
		if (this.controls != null) {
			if (enable) {
				this.controls.animate().translationY(-this.controls.getHeight()).alpha(0F).start();
			} else {
				this.controls.animate().translationY(0).alpha(1F).start();
			}
		}
	}

	/**
	 * Process {@link MotionEvent}s to schedule or cancel fullscreen request
	 * 
	 * @param event
	 *            The touch event
	 * @return <code>true</code> if the event is processed, <code>false</code> otherwise
	 */
	public boolean processMotionEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			schedule();
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return processMotionEvent(event);
	}
}
