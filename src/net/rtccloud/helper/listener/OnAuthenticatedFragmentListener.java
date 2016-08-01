package net.rtccloud.helper.listener;

import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;

/**
 * Interface definition for a callback to be invoked when an action from an authenticated state must be dispatched.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnAuthenticatedFragmentListener extends OnStatusListener {

	/**
	 * Called when the provided {@link Call} becomes {@link CallStatus#ACTIVE}.
	 * 
	 * @param call
	 *            The corresponding {@link Call}.
	 */
	public void onCallActive(Call call);

	/**
	 * Called when the Helper wants to log-out.
	 */
	public void onLogout();
}