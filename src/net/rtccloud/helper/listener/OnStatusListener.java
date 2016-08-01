package net.rtccloud.helper.listener;

import net.rtccloud.helper.controller.StatusBarController;
import net.rtccloud.helper.controller.StatusBarController.StatusBarAction;
import android.app.ActionBar;
import android.os.Bundle;
import android.widget.ProgressBar;


/**
 * Interface definition for a callback to be invoked when the status of the Helper change or when there is a quick {@link StatusBarAction} to display on top of the Helper interface.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnStatusListener {
	
	/**
	 * Called when the Helper needs to update its status with the provided informations.
	 * 
	 * @param title The title to display in the {@link ActionBar}.
	 * @param subtitle The subtitle to display in the {@link ActionBar}.
	 * @param showProgress Whether or not to show a {@link ProgressBar} in the {@link ActionBar}.
	 */
	void onStatusUpdate(String title, String subtitle, boolean showProgress);

	/**
	 * Called when the Helper needs to show the status bar with the provided informations, send to the {@link StatusBarController}.
	 * 
	 * @param title The title to display.
	 * @param action The corresponding action.
	 * @param bundle More details.
	 */
	void onShowStatusBar(String title, StatusBarAction action, Bundle bundle);
}