package net.rtccloud.helper.controller;

import net.rtccloud.helper.fragment.CallFragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


/**
 * Helper class to control and manage the Floating Window touch events inside the {@link CallFragment}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class FloatingWindowTouchController implements OnTouchListener {

	/**
	 * Interface definition for a callback to be invoked when the floating window received touch events
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public interface OnFloatingWindowTouchListener {
		/**
		 * Called when the floating window receive touch events.
		 * 
		 * @param event
		 *            The corresponding touch event.
		 */
		public void onFloatingWindowTouch(MotionEvent event);
	}

	/** The floating window */
	protected View floatingView;
	/** The container of floating window */
	protected View containerView;

	/** Value used to translate the window horizontally */
	protected float x;
	/** Value used to translate the window vertically */
	protected float y;
	/** Initial X coordinate */
	private float initialX;
	/** Initial Y coordinate */
	private float initialY;
	/** X coordinate of the first {@link MotionEvent#ACTION_DOWN} event */
	private float initialTouchX;
	/** Y coordinate of the first {@link MotionEvent#ACTION_DOWN} event */
	private float initialTouchY;

	/** Callback used to dispatch the touch events */
	protected OnFloatingWindowTouchListener callback;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Forward events to the listener
		if (this.callback != null) {
			MotionEvent copy = MotionEvent.obtain(event);
			this.callback.onFloatingWindowTouch(copy);
			copy.recycle();
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.initialX = this.x;
			this.initialY = this.y;
			this.initialTouchX = event.getRawX();
			this.initialTouchY = event.getRawY();
			return true;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			return true;
		case MotionEvent.ACTION_MOVE:
			this.x = fixCoordinateX(this.initialX + (int) (event.getRawX() - this.initialTouchX));
			this.y = fixCoordinateY(this.initialY + (int) (event.getRawY() - this.initialTouchY));
			this.floatingView.setX(this.x);
			this.floatingView.setY(this.y);
			return true;
		default:
			break;
		}
		return false;
	}

	/**
	 * @param xCoordinate
	 *            the requested X coordinate
	 * @return The X coordinate that will fit in the View container
	 */
	protected float fixCoordinateX(float xCoordinate) {
		if (this.floatingView == null || this.containerView == null) {
			return 0;
		}
		int childWidth = this.floatingView.getWidth();
		int parentWidth = this.containerView.getWidth();
		return Math.max(0, Math.min(xCoordinate, parentWidth - childWidth));
	}

	/**
	 * @param yCoordinate
	 *            the requested Y coordinate
	 * @return The Y coordinate that will fit in the View container
	 */
	protected float fixCoordinateY(float yCoordinate) {
		if (this.floatingView == null || this.containerView == null) {
			return 0;
		}
		int childHeight = this.floatingView.getHeight();
		int parentHeight = this.containerView.getHeight();
		return Math.max(0, Math.min(yCoordinate, parentHeight - childHeight));
	}

	/**
	 * Sets the callback to use when touch event occurs
	 * 
	 * @param cb
	 *            The callback
	 */
	public void setCallback(OnFloatingWindowTouchListener cb) {
		this.callback = cb;
	}

	/**
	 * Initialize the {@link FloatingWindowTouchController}
	 * 
	 * @param floating
	 *            The floating view
	 * @param container
	 *            The floating view container
	 */
	public void init(View floating, View container) {
		if (this.floatingView != null) {
			this.floatingView.setOnTouchListener(null);
		}
		this.floatingView = floating;
		this.containerView = container;
		this.floatingView.post(new Runnable() {
			@Override
			public void run() {
				FloatingWindowTouchController.this.x = fixCoordinateX(FloatingWindowTouchController.this.x);
				FloatingWindowTouchController.this.y = fixCoordinateY(FloatingWindowTouchController.this.y);
				FloatingWindowTouchController.this.floatingView.setX(FloatingWindowTouchController.this.x);
				FloatingWindowTouchController.this.floatingView.setY(FloatingWindowTouchController.this.y);
			}
		});
	}

	/**
	 * Start the {@link FloatingWindowTouchController}. The window will react to touch events.
	 */
	public void start() {
		this.x = fixCoordinateX(this.x);
		this.y = fixCoordinateY(this.y);

		this.floatingView.setX(this.x);
		this.floatingView.setY(this.y);

		this.floatingView.setOnTouchListener(this);
	}

	/**
	 * Stop the {@link FloatingWindowTouchController}. The window will no longer react to touch events.
	 */
	public void stop() {
		this.floatingView.setX(0);
		this.floatingView.setY(0);

		this.floatingView.setOnTouchListener(null);
	}
}
