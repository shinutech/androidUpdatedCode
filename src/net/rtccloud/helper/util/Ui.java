package net.rtccloud.helper.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * UI helper class.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class Ui {

	/**
	 * Enable or disable the provided views.
	 * 
	 * @param enable
	 *            The state to apply.
	 * @param views
	 *            The views to enable/disable.
	 */
	public static void enableView(boolean enable, View... views) {
		for (View view : views) {
			view.setEnabled(enable);
			if (view instanceof EditText) {
				view.setFocusable(enable);
				view.setFocusableInTouchMode(enable);
			}
		}
	}

	/**
	 * Set the visibility state of these views.
	 * 
	 * @param visibility
	 *            One of {@link View#VISIBLE}, {@link View#INVISIBLE}, or {@link View#GONE}.
	 * @param views
	 *            The views to use.
	 */
	public static void setVisibility(int visibility, View... views) {
		for (View view : views) {
			view.setVisibility(visibility);
		}
	}

	/**
	 * @param context
	 *            The context to use.
	 * @param whiteDrawableResId
	 *            The drawable to color.
	 * @param targetColor
	 *            The color to apply.
	 * @return The new colored {@link Drawable}.
	 */
	public static Drawable getColoredDrawable(Context context, int whiteDrawableResId, int targetColor) {
		Drawable drawable = context.getResources().getDrawable(whiteDrawableResId);
		ColorFilter filter = new LightingColorFilter(targetColor, 0);
		drawable.mutate().setColorFilter(filter);
		return drawable;
	}

	/**
	 * Hides the SoftKeyboard
	 * 
	 * @param context
	 *            A valid context
	 * @param view
	 *            The view associated with the keyboard
	 */
	public static void hideSoftKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * Request the host {@link Activity} to keep the screen ON or OFF depending of the provided flag.
	 * 
	 * @param window
	 *            The window to apply flags
	 * 
	 * @param keep
	 *            <code>true</code> to keep the screen ON, <code>false</code> to keep the screen OFF.
	 */
	public static void setKeepScreenOn(Window window, boolean keep) {
		if (window == null) {
			return;
		}
		if (keep) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

}
