package com.weemo.phonegap.floating;

/**
 * A SnapLocation corresponds to a location on the screen where the
 * FloatingWindow can be moved.
 */
public enum SnapLocation {

	/** Undefined location */
	UNDEFINED,
	/** Top of the screen */
	TOP,
	/** Bottom of the screen */
	BOTTOM,
	/** Left of the screen */
	LEFT,
	/** Right of the screen */
	RIGHT,
	/** Top left of the screen */
	TOP_LEFT,
	/** Top right of the screen */
	TOP_RIGHT,
	/** Bottom left of the screen */
	BOTTOM_LEFT,
	/** Bottom right of the screen */
	BOTTOM_RIGHT,
	/** Center of the screen */
	CENTER;

	@Override
	public String toString() {
		return this.name();
	}
}
