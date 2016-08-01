package net.rtccloud.helper.listener;

import net.rtccloud.helper.view.CallControls.CallButton;

/**
 * Interface definition for a callback to be invoked when a {@link CallButton} is clicked.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnCallControlClickListener {
	/**
     * Called when a {@link CallButton} has been clicked.
     *
	 * @param button The button that was clicked.
	 */
	public void onCallControlsClick(CallButton button);
}