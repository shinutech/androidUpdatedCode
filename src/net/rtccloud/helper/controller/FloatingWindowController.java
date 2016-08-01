package net.rtccloud.helper.controller;

//import net.rtccloud.helper.R;
import com.synsormed.mobile.R;

import net.rtccloud.helper.controller.FloatingWindowTouchController.OnFloatingWindowTouchListener;
import net.rtccloud.helper.fragment.CallFragment;
import android.app.ActionBar;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;


/**
 * Helper class to control and manage the Floating Window inside the {@link CallFragment}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class FloatingWindowController implements OnFloatingWindowTouchListener {

	/** Action bar to show or hide */
	private ActionBar actionBar;
	/** The floating window */
	protected View floatingView;
	/** The container of floating window */
	protected View containerView;
	/** Flag to detect if the Window is floating */
	protected boolean isFloating = true;

	/** The width of the floating window */
	private final float videoInWidth;
	/** The height of the floating window */
	private final float videoInHeight;

	/** The controller to handle touch event */
	protected FloatingWindowTouchController floatingWindowTouchController;
	/** The controller to handle fullscreen */
	protected FullScreenController fullScreenController;

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            Context used to query dimensions
	 */
	public FloatingWindowController(Context ctx) {
		this.videoInWidth = ctx.getResources().getDimension(R.dimen.video_in_small_size_width);
		this.videoInHeight = ctx.getResources().getDimension(R.dimen.video_in_small_size_height);
		this.floatingWindowTouchController = new FloatingWindowTouchController();
		this.floatingWindowTouchController.setCallback(this);
	}

	/**
	 * Sets the floating window and its container
	 * 
	 * @param floating
	 *            The floating window
	 * @param container
	 *            the floating window container
	 */
	public void init(View floating, View container) {
		this.floatingView = floating;
		this.containerView = container;
		setFloating(this.isFloating);
		if (!this.isFloating) {
			this.floatingView.setVisibility(View.VISIBLE);
		}
		this.floatingWindowTouchController.init(floating, container);
	}

	/**
	 * Sets the {@link ActionBar}
	 * 
	 * @param bar
	 */
	public void setActionBar(ActionBar bar) {
		this.actionBar = bar;
	}

	/**
	 * Sets the fullscreen controller
	 * 
	 * @param controller
	 */
	public void setFullScreenWindowController(FullScreenController controller) {
		this.fullScreenController = controller;
	}

	/**
	 * @return <code>true</code> if the window is floating, <code>false</code> otherwise
	 */
	public boolean isFloating() {
		return this.isFloating;
	}

	/**
	 * Toggle the floating value
	 */
	public void toggle() {
		setFloating(!this.isFloating);
	}

	/**
	 * Change the floating behaviour of the window
	 * 
	 * @param floating
	 *            <code>true</code> to enable the floating window, <code>false</code> otherwise
	 */
	public void setFloating(boolean floating) {
		this.isFloating = floating;
		if (this.actionBar != null) {
			if (this.isFloating) {
				this.actionBar.show();
			} else {
				this.actionBar.hide();
			}
		}

		layout();

		this.floatingView.post(new Runnable() {
			@Override
			public void run() {
				if (FloatingWindowController.this.isFloating) {
					FloatingWindowController.this.floatingWindowTouchController.start();
				} else {
					FloatingWindowController.this.floatingWindowTouchController.stop();
				}
			}
		});
	}

	/**
	 * Request a layout pass to update the floating window size and update its visibility
	 */
	private void layout() {
		if (this.floatingView == null || this.containerView == null) {
			return;
		}

		LayoutParams params = this.floatingView.getLayoutParams();
		params.height = this.isFloating ? (int) this.videoInHeight : LayoutParams.MATCH_PARENT;
		params.width = this.isFloating ? (int) this.videoInWidth : LayoutParams.MATCH_PARENT;
		this.floatingView.setLayoutParams(params);
		this.floatingView.requestLayout();
		if (this.floatingView.getVisibility() != View.VISIBLE) {
			this.floatingView.post(new Runnable() {
				@Override
				public void run() {
					FloatingWindowController.this.floatingView.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onFloatingWindowTouch(MotionEvent event) {
		this.fullScreenController.processMotionEvent(event);
	}
}
