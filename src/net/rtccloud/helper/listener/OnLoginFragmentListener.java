package net.rtccloud.helper.listener;

import net.rtccloud.sdk.event.global.AuthenticatedEvent;

/**
 * Interface definition for a callback to be invoked when a successful {@link AuthenticatedEvent} must be dispatched.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnLoginFragmentListener extends OnStatusListener {

	/**
	 * Called when an {@link AuthenticatedEvent} is successful.
	 */
	public void onAuthenticated();
}