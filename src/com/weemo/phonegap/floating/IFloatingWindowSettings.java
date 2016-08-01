package com.weemo.phonegap.floating;

import android.os.Bundle;

/**
 * Interface to manage settings related to the {@link FloatingWindow}.
 */
public interface IFloatingWindowSettings {

	/**
	 * Should be called to retain the state of the {@link FloatingWindow}.
	 * 
	 * @param outState
	 *            Bundle to store the state.
	 */
	public void onSaveInstanceState(Bundle outState);

	/**
	 * Set whether the user can scale the {@link FloatingWindow}
	 * 
	 * @param enabled
	 *            .
	 */
	public void setScaleEnabled(boolean enabled);

	/**
	 * Set whether the user can drag the {@link FloatingWindow}
	 * 
	 * @param enabled
	 *            .
	 */
	public void setDragEnabled(boolean enabled);

	/**
	 * Set whether the user can drag the {@link FloatingWindow} beyond the
	 * borders of the screen
	 * 
	 * @param enabled
	 *            .
	 */
	public void setBorderEnabled(boolean enabled);

	/**
	 * Set whether the {@link FloatingWindow} will be automatically re-located
	 * to the appropriate corner of its container.
	 * 
	 * @param enabled
	 *            .
	 */
	public void setMagneticBorders(boolean enabled);

	/**
	 * @return Whether the user can scale the {@link FloatingWindow}
	 */
	public boolean isScaleEnabled();

	/**
	 * @return Whether the user can drag the {@link FloatingWindow}
	 */
	public boolean isDragEnabled();

	/**
	 * @return Whether the user can drag the {@link FloatingWindow} beyond the
	 *         borders of the screen
	 */
	public boolean isBorderEnabled();

	/**
	 * @return Whether the {@link FloatingWindow} will be automatically
	 *         re-located to the appropriate corner of its container at the end
	 *         of a drag.
	 */
	public boolean isMagneticBordersEnabled();

}
