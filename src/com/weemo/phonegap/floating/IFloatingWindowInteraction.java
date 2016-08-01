package com.weemo.phonegap.floating;

/**
 * Interface defining interractions the user can apply to the FloatingWindow
 */
public interface IFloatingWindowInteraction {

	/**
	 * Scale to FloatingWindow
	 * 
	 * @param zoomFactor
	 *            a scale of 1f will result the original size
	 */
	public void scale(float zoomFactor);

	/**
	 * Move the FloatingWindow of specified distance.
	 * 
	 * @param x
	 *            horizontal distance in pixels
	 * @param y
	 *            vertical distance in pixels
	 */
	public void dragBy(float x, float y);

	/**
	 * Move the FloatingWindow to a specified location.
	 * 
	 * @param x
	 *            horizontal location in pixels
	 * @param y
	 *            vertical location in pixels
	 * @param animate
	 *            if the drag should be animated
	 * @param forceSanitize
	 *            if the result of the drag should be inside the container.
	 */
	public void dragTo(float x, float y, boolean animate, boolean forceSanitize);

	/**
	 * Snap the FloatingWindow to a specified SnapLocation.
	 * 
	 * @param location
	 *            the new location.
	 * @param animate
	 *            if the snap should be animated
	 */
	public void snapTo(SnapLocation location, boolean animate);

	/**
	 * @return the current scale factor of the FloatingWindow
	 */
	public float getCurrentScale();

	/**
	 * @return the maximum scale factor of the FloatingWindow
	 */
	public float getMaxScale();

	/**
	 * @return the current horizontal position of the FloatingWindow
	 */
	public float getX();

	/**
	 * @return the current vertical position of the FloatingWindow
	 */
	public float getY();

	/**
	 * @return the current getSnapLocation of the FloatingWindow
	 */
	public SnapLocation getSnapLocation();
}
