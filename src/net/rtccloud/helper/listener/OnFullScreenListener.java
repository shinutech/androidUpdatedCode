package net.rtccloud.helper.listener;

/**
 * Interface definition for a callback to be invoked when the Helper needs to be displayed in fullscreen.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public interface OnFullScreenListener {

	/**
	 * Called when the Helper needs to go into fullscreen mode.
	 * 
	 * @param enable
	 *            <code>true</code> to go fullscreen, false otherwise.
	 */
	public void onFullScreen(boolean enable);
}