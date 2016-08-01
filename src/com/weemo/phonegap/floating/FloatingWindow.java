package com.weemo.phonegap.floating;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * The FloatingWindow manage the frame containing
 * the videoIn and the videoOut.
 */
public final class FloatingWindow implements IFloatingWindowInteraction, IFloatingWindowSettings {
	/**
	 * The decelerate factor used with the DecelerateInterpolator
	 */
	private static final float DECELERATE_FACTOR = 2.5f;
	/**
	 * The duration of animations in ms
	 */
	private static final int ANIMATION_DURATION = 500;
	/**
	 * The Tag used for logging
	 */
	private static final String TAG = "FloatingWindow";
	/**
	 * The flag indicating if logging is enabled
	 */
	protected static boolean sDebug = false;

	/** Bundle State key: scale setting (float) */
	private static final String KEY_SETTINGS_SCALE = "KEY_SETTINGS_SCALE";
	/** Bundle State key: drag setting (boolean) */
	private static final String KEY_SETTINGS_DRAG = "KEY_SETTINGS_DRAG";
	/** Bundle State key: border setting (boolean) */
	private static final String KEY_SETTINGS_BORDER = "KEY_SETTINGS_BORDER";
	/** Bundle State key: magnetic borders setting (boolean) */
	private static final String KEY_SETTINGS_MAGNETIC_BORDERS = "KEY_SETTINGS_MAGNETIC_BORDERS";
	/** Bundle State key: snapLocation setting ({@link SnapLocation}) */
	private static final String KEY_SETTINGS_SNAP = "KEY_SETTINGS_SNAP";
	/** Bundle State key: x position in percent (float) */
	private static final String KEY_STATE_PERCENT_X = "KEY_STATE_PERCENT_X";
	/** Bundle State key: y position in percent (float) */
	private static final String KEY_STATE_PERCENT_Y = "KEY_STATE_PERCENT_Y";
	/** Bundle State key: appropriate location ({@link SnapLocation}) */
	private static final String KEY_STATE_MAGNET_LOCATION = "KEY_STATE_MAGNET_LOCATION";
	/** Bundle State key: frame resource identifier (int) */
	private static final String KEY_RES_FRAME_PARENT = "KEY_RES_FRAME_PARENT";
	/** Bundle State key: container resource identifier (int) */
	private static final String KEY_RES_FRAME_CONTAINER = "KEY_RES_FRAME_CONTAINER";
	/** Bundle State key: videoIn resource identifier (int) */
	private static final String KEY_RES_FRAME_IN = "KEY_RES_FRAME_IN";
	/** Bundle State key: videoOut resource identifier (int) */
	private static final String KEY_RES_FRAME_OUT = "KEY_RES_FRAME_OUT";

	/** The default width in px of the container used for scaling and dragging */
	private int DEFAULT_CONTAINER_WIDTH;
	/** The default height in px of the container used for scaling and dragging */
	private int DEFAULT_CONTAINER_HEIGHT;
	/** The parent view (root view) */
	protected View frameParent;
	/** The container view (containing videoIn, videoOut and callControls) */
	protected View frameContainer;
	/** The videoIn view */
	protected View frameIn;
	/** The videoOut view */
	protected View frameOut;
	/** The width in px of the root view */
	protected int parentWidth;
	/** The height in px of the root view */
	protected int parentHeight;
	/** The width in px of the container */
	private int containerWidth;
	/** The height in px of the container */
	private int containerHeight;
	/** The touch events listener (scale, drag, ...) */
	private FloatingWindowListener floatingWindowListener;
	/** The callback to notify events */
	protected OnFloatingWindowListener callback;

	/** Whether the user can scale the {@link FloatingWindow} */
	protected boolean isScaleEnabled;
	/** Whether the user can drag the {@link FloatingWindow} */
	protected boolean isDragEnabled;
	/** Whether the user can drag the {@link FloatingWindow} beyond the borders of the screen */
	protected boolean isBorderEnabled;
	/** Whether the {@link FloatingWindow} will be automatically re-located to the appropriate corner of its container. */
	protected boolean isMagneticBordersEnabled;
	/** The last defined SnapLocation of the {@link FloatingWindow} */
	protected SnapLocation snapLocation = SnapLocation.UNDEFINED;

	/**
	 * Empty constructor. Use the {@link FloatingWindow.Builder} instead.
	 */
	protected FloatingWindow() {
	}

	/**
	 * Enable or disable the debug logging of the {@link FloatingWindow}
	 * 
	 * @param enable
	 *            .
	 */
	public static void enableDebugLogging(boolean enable) {
		sDebug = enable;
	}

	/**
	 * It is the entry point of the {@link FloatingWindow} process. It binds the
	 * {@link FloatingWindowListener} to the frameContainer.
	 * 
	 * @param magneticLocation
	 *            temporary location to use in priority
	 * @param percentX
	 *            temporary X value used if there is no snapLocation defined
	 * @param percentY
	 *            temporary Y value used if there is no snapLocation defined
	 */
	protected void attachListener(final SnapLocation magneticLocation, final float percentX, final float percentY) {

		// Detect layout changes
		((View) frameParent.getParent()).addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (parentHeight != frameParent.getHeight() || parentWidth != frameParent.getWidth()) {
					if (sDebug) {
						Log.d(TAG, "onLayoutChange");
					}
					updateValues(findAppropriateLocation(), percentX, percentY);
				}
			}
		});

		this.DEFAULT_CONTAINER_WIDTH = frameContainer.getWidth();
		this.DEFAULT_CONTAINER_HEIGHT = frameContainer.getHeight();

		updateValues(magneticLocation, percentX, percentY);

		if (callback != null) {
			callback.onPostInit();
		}
	}

	/**
	 * Force the {@link FloatingWindow} to update theses values.
	 * Use this when you detect changes in layout.
	 * @param magneticLocation the new snapLocation
	 * @param percentX the new X coordinate in percent
	 * @param percentY the new Y coordinate in percent
	 */
	protected void updateValues(final SnapLocation magneticLocation, final float percentX, final float percentY) {
		containerWidth = frameContainer.getWidth();
		containerHeight = frameContainer.getHeight();
		if (sDebug) {
			Log.d(TAG, String.format("container size is w:%d h:%d", containerWidth, containerHeight));
		}

		parentHeight = frameParent.getHeight();
		parentWidth = frameParent.getWidth();
		if (sDebug) {
			Log.d(TAG, String.format("containerParent size is w:%d h:%d", parentWidth, parentHeight));
		}

		float previousScale = 1f;
		float maxScale = Math.max(0f, Math.min((float) parentWidth / (float) DEFAULT_CONTAINER_WIDTH, (float) parentHeight / (float) DEFAULT_CONTAINER_HEIGHT));
		if (floatingWindowListener != null) {
			previousScale = floatingWindowListener.getCurrentScale();
		}
		floatingWindowListener = new FloatingWindowListener(this, frameParent.getContext());
		floatingWindowListener.setMaxScale(maxScale);
		floatingWindowListener.setCurrentScale(Math.min(previousScale, maxScale));

		frameContainer.setOnTouchListener(floatingWindowListener);

		if (isMagneticBordersEnabled && magneticLocation != null && magneticLocation != SnapLocation.UNDEFINED) {
			snapTo(magneticLocation, false);
		} else {
			if (snapLocation == null || snapLocation == SnapLocation.UNDEFINED) {
				dragTo(parentWidth * percentX - (float) containerWidth / 2, parentHeight * percentY - (float) containerHeight / 2, false, true);
			} else {
				snapTo(snapLocation, false);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (outState == null) {
			outState = new Bundle();
		}
		outState.putBoolean(KEY_SETTINGS_BORDER, isBorderEnabled);
		outState.putBoolean(KEY_SETTINGS_MAGNETIC_BORDERS, isMagneticBordersEnabled);
		outState.putBoolean(KEY_SETTINGS_DRAG, isDragEnabled);
		outState.putBoolean(KEY_SETTINGS_SCALE, isScaleEnabled);
		outState.putSerializable(KEY_SETTINGS_SNAP, snapLocation);
		if (isMagneticBordersEnabled) {
			outState.putSerializable(KEY_STATE_MAGNET_LOCATION, findAppropriateLocation());
		}
		if (snapLocation == SnapLocation.UNDEFINED) {
			outState.putFloat(KEY_STATE_PERCENT_X, (frameContainer.getX() + (float) containerWidth / 2) / (parentWidth));
			outState.putFloat(KEY_STATE_PERCENT_Y, (frameContainer.getY() + (float) containerHeight / 2) / (parentHeight));
		}
		outState.putInt(KEY_RES_FRAME_PARENT, frameParent.getId());
		outState.putInt(KEY_RES_FRAME_CONTAINER, frameContainer.getId());
		outState.putInt(KEY_RES_FRAME_IN, frameIn.getId());
		outState.putInt(KEY_RES_FRAME_OUT, frameOut.getId());
	}

	@Override
	public void scale(float zoomFactor) {
		if (sDebug) {
			Log.d(TAG, String.format("scale(%f)", zoomFactor));
		}

		ViewGroup.LayoutParams paramsContainer = frameContainer.getLayoutParams();

		assert paramsContainer != null;

		int oldWidthIn = paramsContainer.width;
		int oldHeightIn = paramsContainer.height;
		int newWidthIn = (int) (DEFAULT_CONTAINER_WIDTH * zoomFactor);
		int newHeightIn = (int) (DEFAULT_CONTAINER_HEIGHT * zoomFactor);
		final int diffWidthIn = newWidthIn - oldWidthIn;
		final int diffHeightIn = newHeightIn - oldHeightIn;

		if (diffWidthIn == 0 || diffHeightIn == 0) {
			return;
		}

		paramsContainer.width = newWidthIn;
		paramsContainer.height = newHeightIn;
		containerWidth = newWidthIn;
		containerHeight = newHeightIn;

		if (sDebug) {
			Log.d(TAG, String.format("VideoIn  oldWidth:%d, oldHeight:%d, newWidth:%d, newHeight:%d, diffWidth:%d, diffHeight:%d", oldWidthIn, oldHeightIn, newWidthIn, newHeightIn, diffWidthIn,
					diffHeightIn));
		}

		frameContainer.setLayoutParams(paramsContainer);
		floatingWindowListener.setCurrentScale(zoomFactor);
		dragTo(frameContainer.getX() - (float) diffWidthIn / 2, frameContainer.getY() - (float) diffHeightIn / 2, false, false);
	}

	@Override
	public void dragBy(float x, float y) {
		if (sDebug) {
			Log.d(TAG, String.format("dragBy(%f, %f)", x, y));
		}

		frameContainer.setX(isBorderEnabled ? sanitizeX(frameContainer.getX() + x) : frameContainer.getX() + x);
		frameContainer.setY(isBorderEnabled ? sanitizeY(frameContainer.getY() + y) : frameContainer.getY() + y);
	}

	@Override
	public void dragTo(float x, float y, boolean animate, boolean forceSanitize) {
		if (sDebug) {
			Log.d(TAG, String.format("dragTo(%f, %f, %s, %s)", x, y, animate, forceSanitize));
		}
		if (!animate) {
			frameContainer.setX(forceSanitize || isBorderEnabled ? sanitizeX(x) : x);
			frameContainer.setY(forceSanitize || isBorderEnabled ? sanitizeY(y) : y);
		} else {
			animateXY(forceSanitize || isBorderEnabled ? sanitizeX(x) : x, forceSanitize || isBorderEnabled ? sanitizeY(y) : y);
		}
	}

	@Override
	public void snapTo(SnapLocation location, boolean animate) {
		if (sDebug) {
			Log.d(TAG, String.format("snapTo(%s, %s)", location.name(), animate));
		}
		snapLocation = location;

		if (location == SnapLocation.UNDEFINED) {
			return;
		}
		final float currentX = frameContainer.getX();
		final float currentY = frameContainer.getY();
		ViewGroup.LayoutParams params = frameContainer.getLayoutParams();
		float newX = findNewX(location, params);
		float newY = findNewY(location, params);

		if (newX != currentX || newY != currentY) {
			dragTo(newX, newY, animate, true);
		}
	}

	/**
	 * Move the {@link FloatingWindow} to be completely visible by the user 
	 */
	public void fixPositionIfNeeded() {
		if (sDebug) {
			Log.d(TAG, "fixPositionIfNeeded()");
		}

		if (isMagneticBordersEnabled) {
			SnapLocation newLocation = findAppropriateLocation();
			final float currentX = frameContainer.getX();
			final float currentY = frameContainer.getY();
			ViewGroup.LayoutParams params = frameContainer.getLayoutParams();
			float newX = findNewX(newLocation, params);
			float newY = findNewY(newLocation, params);

			if (newX != currentX || newY != currentY) {
				dragTo(newX, newY, true, true);
			}
		} else {
			if (frameContainer.getX() != sanitizeX(frameContainer.getX()) || frameContainer.getY() != sanitizeY(frameContainer.getY())) {
				if (sDebug) {
					Log.d(TAG, "Position need to be fixed");
				}
				dragTo(frameContainer.getX(), frameContainer.getY(), true, true);
			}
		}
	}

	/**
	 * Cancels the current animation on the frameContainer
	 */
	protected void cancelAnimations() {
		frameContainer.animate().cancel();
	}

	/**
	 * Animate the {@link FloatingWindow} to the supplied x and y coordinates
	 * @param x the new X coordinate
	 * @param y the new T coordinate
	 */
	private void animateXY(final float x, final float y) {
		if (sDebug) {
			Log.d(TAG, String.format("animateXY(%f, %f)", x, y));
		}
		frameContainer.animate().cancel();
		frameContainer.animate().translationX(x).translationY(y).setDuration(ANIMATION_DURATION).setInterpolator(new DecelerateInterpolator(DECELERATE_FACTOR)).start();
	}

	/**
	 * @return the appropriate {@link SnapLocation} according to the current location of the {@link FloatingWindow}
	 */
	protected SnapLocation findAppropriateLocation() {
		final float x = frameContainer.getX();
		final float y = frameContainer.getY();
		float centerX = (x + (float) containerWidth / 2) / (parentWidth);
		float centerY = (y + (float) containerHeight / 2) / (parentHeight);
		float first = 1f / 3f;
		float second = 2f / 3f;

		boolean top = y <= 0 || centerY < first;
		boolean bottom = y >= parentHeight - containerHeight || centerY > second;
		boolean left = x <= 0 || centerX < first;
		boolean right = x >= parentWidth - containerWidth || centerX > second;
		boolean center = centerX >= first && first <= second && centerY >= first && centerY <= second;

		if (top && left) {
			return SnapLocation.TOP_LEFT;
		} else if (top && right) {
			return SnapLocation.TOP_RIGHT;
		} else if (bottom && right) {
			return SnapLocation.BOTTOM_RIGHT;
		} else if (bottom && left) {
			return SnapLocation.BOTTOM_LEFT;
		} else if (top) {
			return SnapLocation.TOP;
		} else if (left) {
			return SnapLocation.LEFT;
		} else if (right) {
			return SnapLocation.RIGHT;
		} else if (bottom) {
			return SnapLocation.BOTTOM;
		} else if (center) {
			return SnapLocation.CENTER;
		} else {
			return SnapLocation.UNDEFINED;
		}
	}

	/**
	 * @param location the {@link SnapLocation} to test
	 * @param params the params of the {@link FloatingWindow}
	 * @return The new X coordinate if a snap is performed
	 */
	private float findNewX(SnapLocation location, ViewGroup.LayoutParams params) {
		switch (location) {
		case BOTTOM_LEFT:
		case LEFT:
		case TOP_LEFT:
			return 0;
		case BOTTOM_RIGHT:
		case TOP_RIGHT:
		case RIGHT:
			return parentWidth - params.width;
		case CENTER:
		case TOP:
		case BOTTOM:
			return (float) parentWidth / 2 - (float) params.width / 2;
		default:
			return frameContainer.getX();
		}
	}
	
	/**
	 * @param location the SnapLocation to test
	 * @param params the params of the {@link FloatingWindow}
	 * @return The new Y coordinate if a snap is performed
	 */
	private float findNewY(SnapLocation location, ViewGroup.LayoutParams params) {
		switch (location) {
		case BOTTOM:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
			return parentHeight - params.height;
		case TOP:
		case TOP_LEFT:
		case TOP_RIGHT:
			return 0;
		case CENTER:
		case LEFT:
		case RIGHT:
			return (float) parentHeight / 2 - (float) params.height / 2;
		default:
			return frameContainer.getY();
		}
	}

	/**
	 * @param x The X coordinate in px
	 * @return the X coordinate inside the root frame 
	 */
	private float sanitizeX(float x) {
		return Math.max(0, Math.min(x, parentWidth - containerWidth));
	}

	/**
	 * @param y The Y coordinate in px
	 * @return the Y coordinate inside the root frame
	 */
	private float sanitizeY(float y) {
		return Math.max(0, Math.min(y, parentHeight - containerHeight));
	}

	@Override
	public void setMagneticBorders(boolean enabled) {
		isMagneticBordersEnabled = enabled;
	}

	@Override
	public boolean isScaleEnabled() {
		return isScaleEnabled;
	}

	@Override
	public void setScaleEnabled(boolean enabled) {
		isScaleEnabled = enabled;
	}

	@Override
	public boolean isDragEnabled() {
		return isDragEnabled;
	}

	@Override
	public void setDragEnabled(boolean enabled) {
		isDragEnabled = enabled;
	}

	@Override
	public boolean isBorderEnabled() {
		return isBorderEnabled;
	}

	@Override
	public void setBorderEnabled(boolean enabled) {
		isBorderEnabled = enabled;
	}

	@Override
	public boolean isMagneticBordersEnabled() {
		return isMagneticBordersEnabled;
	}

	@Override
	public float getCurrentScale() {
		return floatingWindowListener.getCurrentScale();
	}

	@Override
	public float getMaxScale() {
		return floatingWindowListener.getMaxScale();
	}

	@Override
	public float getX() {
		return frameContainer.getX();
	}

	@Override
	public float getY() {
		return frameContainer.getY();
	}

	@Override
	public SnapLocation getSnapLocation() {
		return snapLocation;
	}

	/**
	 * Interface definition for a callback to be invoked when a {@link FloatingWindow} event is occuring .
	 */
	public interface OnFloatingWindowListener {
		/** Called when the {@link FloatingWindow} is created and ready to use */
		public void onPostInit();
		/** Called when the user perform a touch up event on the {@link FloatingWindow} */
		public void onTouchUp();
		/** Called when the user perform a touch down event on the {@link FloatingWindow} */
		public void onTouchDown();
	}

	/**
	 * Builder pattern to help creating the {@link FloatingWindow}.
	 */
	public static class Builder {

		/** The activity containing the {@link FloatingWindow} */
		private Activity activity;
		/** The view root containing the {@link FloatingWindow} */
		private View view;
		/** The Callback to notify events */
		private OnFloatingWindowListener callback;

		/** Whether the user can scale the {@link FloatingWindow} */
		private boolean isScaleEnabled = false;
		/** Whether the user can drag the {@link FloatingWindow} */
		private boolean isDragEnabled = true;
		/** Whether the user can drag the {@link FloatingWindow} beyond the borders of the screen */
		private boolean isBorderEnabled = false;
		/** Whether the {@link FloatingWindow} will be automatically move to the approriate SnapLocation at the end of a drag. */
		private boolean isMagneticBordersEnabled = true;
		/** The SnapLocation of the {@link FloatingWindow} */
		private SnapLocation location = SnapLocation.UNDEFINED;
		/** The X position of the {@link FloatingWindow} in percent */
		protected float percentX;
		/** The Y position of the {@link FloatingWindow} in percent */
		protected float percentY;
		/** The location of the {@link FloatingWindow} */
		protected SnapLocation magneticLocation;

		/** The resource identifier of the root of the fragment */
		private int resFrameParent;
		/** The resource identifier of the Frame containing videoIn, videoOut, and the callControls */
		private int resFrameContainer;
		/** The resource identifier of the videoIn */
		private int resFrameIn;
		/** The resource identifier of the videoOut */
		private int resFrameOut;

		/**
		 * Constructor using an Activity.
		 * 
		 * @param activity
		 *            .
		 */
		public Builder(Activity activity) {
			if (activity == null) {
				throw new IllegalArgumentException("You should pass an Activity that is not null");
			}
			this.activity = activity;
		}

		/**
		 * Constructor using a view.
		 * 
		 * @param view
		 *            .
		 */
		public Builder(View view) {
			if (view == null) {
				throw new IllegalArgumentException("You should pass a View that is not null");
			}
			this.view = view;
		}

		/**
		 * Set the resource identifiers for the {@link FloatingWindow} components
		 * 
		 * @param frameParent
		 *            the root frame containing the frameContainer
		 * @param frameContainer
		 *            the frame containing the video in and the video out
		 * @param frameIn
		 *            the frame containing the video in
		 * @param frameOut
		 *            the frame containing the video out
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder withIdentifiers(int frameParent, int frameContainer, int frameIn, int frameOut) {
			this.resFrameParent = frameParent;
			this.resFrameContainer = frameContainer;
			this.resFrameIn = frameIn;
			this.resFrameOut = frameOut;
			return this;
		}

		/**
		 * Set whether the {@link FloatingWindow} can be scaled.
		 * 
		 * @param enabled
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder setScaleEnabled(boolean enabled) {
			isScaleEnabled = enabled;
			return this;
		}

		/**
		 * Set whether the {@link FloatingWindow} can be dragged/moved.
		 * 
		 * @param enabled
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder setDragEnabled(boolean enabled) {
			isDragEnabled = enabled;
			return this;
		}

		/**
		 * Set whether the {@link FloatingWindow} can move across its container.
		 * 
		 * @param enabled
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder setBorderEnabled(boolean enabled) {
			isBorderEnabled = enabled;
			return this;
		}

		/**
		 * Set whether the {@link FloatingWindow} will be automatically re-located to
		 * the appropriate corner of its container.
		 * 
		 * @param enabled
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder setMagneticBordersEnabled(boolean enabled) {
			isMagneticBordersEnabled = enabled;
			return this;
		}

		/**
		 * Set a callback to be notified when the init phase is done
		 * 
		 * @param callback
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder withCallback(OnFloatingWindowListener callback) {
			this.callback = callback;
			return this;
		}

		/**
		 * Set the snapLocation of the {@link FloatingWindow}.
		 * 
		 * @param location
		 *            the new location.
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder snapTo(SnapLocation location) {
			this.location = location;
			return this;
		}

		/**
		 * Configure the {@link FloatingWindow} with parameters stored in the Bundle
		 * 
		 * @param savedInstanceState
		 *            .
		 * @return This Builder object to allow for chaining of calls
		 */
		public Builder withSavedInstance(Bundle savedInstanceState) {
			if (savedInstanceState == null) {
				throw new IllegalArgumentException("The savedInstanceState Bundle should not be null.");
			}
			this.isBorderEnabled = savedInstanceState.getBoolean(KEY_SETTINGS_BORDER, false);
			this.isMagneticBordersEnabled = savedInstanceState.getBoolean(KEY_SETTINGS_MAGNETIC_BORDERS, false);
			this.isDragEnabled = savedInstanceState.getBoolean(KEY_SETTINGS_DRAG, true);
			this.isScaleEnabled = savedInstanceState.getBoolean(KEY_SETTINGS_SCALE, false);
			this.location = (SnapLocation) savedInstanceState.getSerializable(KEY_SETTINGS_SNAP);
			this.magneticLocation = (SnapLocation) savedInstanceState.getSerializable(KEY_STATE_MAGNET_LOCATION);
			if (this.location == null || this.location == SnapLocation.UNDEFINED) {
				this.location = SnapLocation.UNDEFINED;
				this.percentX = savedInstanceState.getFloat(KEY_STATE_PERCENT_X);
				this.percentY = savedInstanceState.getFloat(KEY_STATE_PERCENT_Y);
			}
			this.resFrameParent = savedInstanceState.getInt(KEY_RES_FRAME_PARENT);
			this.resFrameContainer = savedInstanceState.getInt(KEY_RES_FRAME_CONTAINER);
			this.resFrameIn = savedInstanceState.getInt(KEY_RES_FRAME_IN);
			this.resFrameOut = savedInstanceState.getInt(KEY_RES_FRAME_OUT);
			return this;
		}

		/**
		 * Creates a {@link FloatingWindow} with the arguments supplied to this builder.
		 * 
		 * @return the FloatingWindow
		 */
		public FloatingWindow build() {
			final FloatingWindow floatingWindow = new FloatingWindow();
			floatingWindow.callback = callback;
			if (activity != null) {
				floatingWindow.frameParent = activity.findViewById(resFrameParent);
				floatingWindow.frameContainer = activity.findViewById(resFrameContainer);
				floatingWindow.frameIn = activity.findViewById(resFrameIn);
				floatingWindow.frameOut = activity.findViewById(resFrameOut);
			} else if (view != null) {
				floatingWindow.frameParent = view.findViewById(resFrameParent);
				floatingWindow.frameContainer = view.findViewById(resFrameContainer);
				floatingWindow.frameIn = view.findViewById(resFrameIn);
				floatingWindow.frameOut = view.findViewById(resFrameOut);
			} else {
				throw new IllegalArgumentException("You should either pass an Activity or a View that is not null");
			}

			if (floatingWindow.frameParent == null) {
				throw new IllegalArgumentException(String.format("Unable to find the `frameParent` with the corresponding identifier `%d`", resFrameParent));
			}
			if (floatingWindow.frameContainer == null) {
				throw new IllegalArgumentException(String.format("Unable to find the `resFrameContainer` with the corresponding identifier `%d`", resFrameContainer));
			}
			if (floatingWindow.frameIn == null) {
				throw new IllegalArgumentException(String.format("Unable to find the `resFrameIn` with the corresponding identifier `%d`", resFrameIn));
			}
			if (floatingWindow.frameOut == null) {
				throw new IllegalArgumentException(String.format("Unable to find the `resFrameOut` with the corresponding identifier `%d`", resFrameOut));
			}

			floatingWindow.isScaleEnabled = isScaleEnabled;
			floatingWindow.isDragEnabled = isDragEnabled;
			floatingWindow.isBorderEnabled = isBorderEnabled;
			floatingWindow.isMagneticBordersEnabled = isMagneticBordersEnabled;
			floatingWindow.snapLocation = location;

			floatingWindow.frameParent.post(new Runnable() {
				@Override
				public void run() {
					floatingWindow.attachListener(magneticLocation, percentX, percentY);
				}
			});
			return floatingWindow;
		}
	}
}
