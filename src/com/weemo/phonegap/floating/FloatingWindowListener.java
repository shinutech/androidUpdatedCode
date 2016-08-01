package com.weemo.phonegap.floating;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * This class is responsible of dispatching touch events and scale events
 * through the callback.
 */
public final class FloatingWindowListener implements View.OnTouchListener {

	/**
	 * The Tag used for logging
	 */
	private static final String TAG = "FloatingWindowListener";

	/**
	 * The Callback to dispatch events
	 */
	private final FloatingWindow floatingWindow;

	/**
	 * The scale event listener.
	 */
	private final ScaleGestureDetector scaleDetector;

	/**
	 * X coordinate of the last touch event
	 */
	private float mLastTouchX;
	/**
	 * Y coordinate of the last touch event
	 */
	private float mLastTouchY;
	/**
	 * Id of the active pointer
	 */
	private int mActivePointerId;

	/**
	 * The minimum threshold needed to detect a scale gesture
	 */
	private static final float SCALE_THRESHOLD = 0.005f;

	/**
	 * The current scale factor of the FloatingWindow
	 */
	protected float currentScaleFactor = 1.f;

	/**
	 * The maximum scale factor of the FloatingWindow
	 */
	protected float maxScaleFactor = 1.f;

	/**
	 * Creates a FloatingWindowListener with the supplied FloatingWindow.
	 * 
	 * @param floatingWindow
	 *            the FloatingWindow callback
	 * @param ctx
	 *            The Context
	 */
	public FloatingWindowListener(final FloatingWindow floatingWindow, Context ctx) {
		this.floatingWindow = floatingWindow;
		scaleDetector = new ScaleGestureDetector(ctx, new ScaleGestureDetector.OnScaleGestureListener() {

			@Override
			public void onScaleEnd(ScaleGestureDetector arg0) {
				if (FloatingWindow.sDebug) {
					Log.d(TAG, "onScaleEnd()");
				}
				floatingWindow.fixPositionIfNeeded();
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector arg0) {
				return true;
			}

			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				if (FloatingWindow.sDebug) {
					Log.d(TAG,
							String.format("onScale(%f)  --> %f  diff:%f", detector.getScaleFactor(), currentScaleFactor * detector.getScaleFactor(),
									Math.abs(currentScaleFactor * detector.getScaleFactor() - currentScaleFactor)));
				}
				if (Math.abs(currentScaleFactor * detector.getScaleFactor() - currentScaleFactor) > SCALE_THRESHOLD) {
					currentScaleFactor = Math.max(1f, Math.min(currentScaleFactor * detector.getScaleFactor(), maxScaleFactor));
					floatingWindow.scale(currentScaleFactor);
					return true;
				}

				// Let's accumulate the scale factor
				return false;
			}
		});
	}

	/**
	 * Called when a touch event is dispatched to a view. This allows listeners
	 * to get a chance to respond before the target view.
	 * 
	 * @param v
	 *            The view the touch event has been dispatched to.
	 * @param event
	 *            The MotionEvent object containing full information about the
	 *            event.
	 * @return True if the listener has consumed the event, false otherwise.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (FloatingWindow.sDebug) {
			debugEvent(event);
		}

		int actionMasked = MotionEventCompat.getActionMasked(event);

		if (floatingWindow.isScaleEnabled()) {
			scaleDetector.onTouchEvent(event);
			if (scaleDetector.isInProgress()) {
				final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
				if (mActivePointerId != MotionEvent.INVALID_POINTER_ID) {
					mLastTouchX = MotionEventCompat.getX(event, pointerIndex);
					mLastTouchY = MotionEventCompat.getY(event, pointerIndex);
				}
				return true;
			}
		}

		if (floatingWindow.callback != null) {
			switch (actionMasked) {
			case MotionEvent.ACTION_DOWN:
				floatingWindow.callback.onTouchDown();
				break;
			case MotionEvent.ACTION_UP:
				floatingWindow.callback.onTouchUp();
				break;
			default:
				// Do Nothing
				break;
			}
		}

		if (!floatingWindow.isDragEnabled()) {
			return true;
		}

		switch (actionMasked) {
		case MotionEvent.ACTION_DOWN: {
			floatingWindow.cancelAnimations();
			final int pointerIndex = MotionEventCompat.getActionIndex(event);
			final float x = MotionEventCompat.getX(event, pointerIndex);
			final float y = MotionEventCompat.getY(event, pointerIndex);
			mLastTouchX = x;
			mLastTouchY = y;
			mActivePointerId = MotionEventCompat.getPointerId(event, 0);
			return true;
		}

		case MotionEvent.ACTION_MOVE: {
			floatingWindow.cancelAnimations();
			final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
			if (mActivePointerId == MotionEvent.INVALID_POINTER_ID) {
				return true;
			}

			final float x = MotionEventCompat.getX(event, pointerIndex);
			final float y = MotionEventCompat.getY(event, pointerIndex);

			// Calculate the distance moved
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;

			floatingWindow.dragBy(dx, dy);

			return true;
		}

		case MotionEvent.ACTION_UP: {
			mActivePointerId = MotionEvent.INVALID_POINTER_ID;
			if (!scaleDetector.isInProgress()) {
				floatingWindow.fixPositionIfNeeded();
			}
			return true;
		}

		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = MotionEvent.INVALID_POINTER_ID;
			floatingWindow.fixPositionIfNeeded();
			return true;
		}

		case MotionEvent.ACTION_OUTSIDE: {
			mActivePointerId = MotionEvent.INVALID_POINTER_ID;
			floatingWindow.fixPositionIfNeeded();
			return true;
		}

		case MotionEvent.ACTION_POINTER_UP: {

			final int pointerIndex = MotionEventCompat.getActionIndex(event);
			final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

			if (pointerId == mActivePointerId) {
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = MotionEventCompat.getX(event, newPointerIndex);
				mLastTouchY = MotionEventCompat.getY(event, newPointerIndex);
				mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
			}
			return true;
		}
		}

		return false;
	}

	/**
	 * Debug the supplied MotionEvent
	 * 
	 * @param event
	 *            the event to debug
	 */
	private void debugEvent(final MotionEvent event) {
		String action;
		switch (MotionEventCompat.getActionMasked(event)) {
		case MotionEvent.ACTION_CANCEL:
			action = "-> ACTION_CANCEL";
			break;
		case MotionEvent.ACTION_DOWN:
			action = "-> ACTION_DOWN";
			break;
		case MotionEvent.ACTION_MOVE:
			action = "-> ACTION_MOVE";
			break;
		case MotionEvent.ACTION_OUTSIDE:
			action = "-> ACTION_OUTSIDE";
			break;
		case MotionEvent.ACTION_UP:
			action = "-> ACTION_UP";
			break;
		case MotionEvent.ACTION_POINTER_UP:
			action = "-> ACTION_POINTER_UP";
			break;
		default:
			action = "-> undefined";
			break;
		}

		Log.d(TAG, String.format("onTouch(%d)  %s  x:%f y:%f", event.getEventTime(), action, event.getX(), event.getY()));
	}

	/**
	 * Set the maximum scale factor of the FloatingWindow
	 * 
	 * @param maxScaleFactor
	 *            .
	 */
	public void setMaxScale(float maxScaleFactor) {
		if (FloatingWindow.sDebug) {
			Log.d(TAG, String.format("maxScaleFactor is now %f", maxScaleFactor));
		}
		this.maxScaleFactor = maxScaleFactor;
	}

	/**
	 * @return The current scale factor of the FloatingWindow
	 */
	public float getCurrentScale() {
		return currentScaleFactor;
	}

	/**
	 * @return The maximum scale factor of the FloatingWindow
	 */
	public float getMaxScale() {
		return maxScaleFactor;
	}

	/**
	 * Set the current scale factor of the FloatingWindow
	 * 
	 * @param scale
	 *            the new scale factor
	 */
	public void setCurrentScale(float scale) {
		this.currentScaleFactor = scale;
	}
}
