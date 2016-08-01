package net.rtccloud.helper.listener;

import android.support.v4.app.ActionBarDrawerToggle;

import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;

/**
 * Interface definition for a callback to be invoked when a {@link Call} has ended.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnCallFragmentListener extends OnStatusListener {
	/**
	 * Called when the provided {@link Call} becomes {@link CallStatus#ENDED}.
	 * 
	 * @param call
	 *            The corresponding {@link Call}.
	 */
	void onHangup(Call call);
	
	/**
	 * Called when the caller needs to display the {@link ActionBarDrawerToggle}.
	 * 
	 * @param enable
	 *            <code>true</code> to display the {@link ActionBarDrawerToggle}, <code>false</code> otherwise.
	 */
	void enableDrawerToggle(boolean enable);
}