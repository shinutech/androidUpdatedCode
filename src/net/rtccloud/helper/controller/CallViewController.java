package net.rtccloud.helper.controller;

import com.synsormed.mobile.R;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

//import net.rtccloud.helper.R;
import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.view.CallControls;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.view.VideoInFrame;
import net.rtccloud.sdk.view.VideoOutPreviewFrame;

/**
 * Helper class to control and manage the call view inside the {@link CallFragment}
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class CallViewController {

	/** The call */
	private Call call;
	/** The floating window controller */
	private FloatingWindowController floatingWindowController;

	/** Self view */
	private VideoOutPreviewFrame videoSelf;
	/** The call controls */
	private CallControls controls;

	/** Master Video frame */
	private VideoInFrame videoMaster;
	/** Master Label view */
	private TextView labelMaster;

	/** Thumbnail Video frames */
	private VideoInFrame[] videoThumbnails;
	/** Thumbnail Label views */
	private TextView[] labelThumbnails;

	/** Regular {@link VideoOutPreviewFrame} margin */
	private final int dimenVideoSelfMargin;
	/** Fullscreen {@link VideoOutPreviewFrame} margin */
	private final int dimenVideoSelfMarginFullscreen;
	/** Regular {@link VideoOutPreviewFrame} size */
	private final int dimenVideoSelfSize;
	/** Fullscreen {@link VideoOutPreviewFrame} size */
	private final int dimenVideoSelfSizeFullscreen;
	/** Regular {@link VideoInFrame} thumbnail size */
	private final int dimenThumnailSize;
	/** Fullscreen {@link VideoInFrame} thumbnail size */
	private final int dimenThumnailSizeFullscreen;

	/** If the Video Out was paused (while going to background) */
	private boolean paused = false;

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            The context used to get dimensions
	 */
	public CallViewController(Context ctx) {
		Resources r = ctx.getResources();

		this.dimenVideoSelfMargin = (int) r.getDimension(R.dimen.video_out_margin);
		this.dimenVideoSelfMarginFullscreen = (int) r.getDimension(R.dimen.video_out_margin_fullscreen);
		this.dimenVideoSelfSize = (int) r.getDimension(R.dimen.video_out_size);
		this.dimenVideoSelfSizeFullscreen = (int) r.getDimension(R.dimen.video_out_size_fullscreen);
		this.dimenThumnailSize = (int) r.getDimension(R.dimen.video_thumbnail_size);
		this.dimenThumnailSizeFullscreen = (int) r.getDimension(R.dimen.video_thumbnail_size_fullscreen);
	}

	/**
	 * Sets the {@link Call}
	 * 
	 * @param call
	 */
	public void setCall(Call call) {
		this.call = call;
	}

	/**
	 * Sets the floating window controller
	 * 
	 * @param controller
	 */
	public void setFloatingWindowController(FloatingWindowController controller) {
		this.floatingWindowController = controller;
	}

	/**
	 * Sets the Master Video frame and the Thumbnail Video frames
	 * 
	 * @param controls
	 *            The call controls
	 * @param self
	 *            The preview frame
	 * @param master
	 *            The master video in
	 * @param thumbnails
	 *            The thumbnails video in
	 * 
	 */
	public void setVideoViews(CallControls controls, VideoOutPreviewFrame self, VideoInFrame master, VideoInFrame... thumbnails) {
		this.videoSelf = self;
		this.videoMaster = master;
		this.videoThumbnails = thumbnails;
		this.controls = controls;
	}

	/**
	 * Sets the Master Label view and the Thumbail Label views
	 * 
	 * @param master
	 * @param thumbnails
	 */
	public void setLabelViews(TextView master, TextView... thumbnails) {
		this.labelMaster = master;
		this.labelThumbnails = thumbnails;
	}

	/**
	 * Helper method up call multiple methods at once.
	 * 
	 * @param updateContactReferences
	 *            <code>true</code> to call this method, <code>false</code> otherwise
	 * @param updateCallControls
	 *            <code>true</code> to call this method, <code>false</code> otherwise
	 * @param updateViewSizes
	 *            <code>true</code> to call this method, <code>false</code> otherwise
	 */
	public void update(boolean updateContactReferences, boolean updateCallControls, boolean updateViewSizes) {
		if (updateContactReferences) {
			updateContactReferences();
		}
		if (updateCallControls) {
			updateCallControls();
		}
		if (updateViewSizes) {
			updateViewSizes();
		}
	}

	/**
	 * Update the size of thumbnails according to the floating window state
	 * 
	 * @param floatingWindow
	 */
	private void updateThumbnails(boolean floatingWindow) {
		if (this.videoThumbnails == null || this.videoThumbnails.length == 0) {
			return;
		}

		final int size = floatingWindow ? this.dimenThumnailSize : this.dimenThumnailSizeFullscreen;
		for (int i = 0; i < this.videoThumbnails.length; i++) {
			View thumbnail = this.videoThumbnails[i];
			if (thumbnail != null) {
				LayoutParams thumbnailParam = thumbnail.getLayoutParams();
				thumbnailParam.width = size;
				thumbnailParam.height = size;
				thumbnail.setLayoutParams(thumbnailParam);
			}
		}
	}

	/**
	 * Update the views used by participants. Must be called as soon as the floor list or participant list changes.<br />
	 * This method will:
	 * <ul>
	 * <li>Update View references by calling {@link Contact#setView(VideoInFrame)} on each participant</li>
	 * <li>Update Labels displaying contact display names</li>
	 * <li>Hide unused thumbnails</li>
	 * </ul>
	 */
	private void updateContactReferences() {
		if (this.call == null || this.call.getStatus() == CallStatus.ENDED) {
			return;
		}
		final boolean isFloating = this.floatingWindowController.isFloating();
		if (this.call.isConference()) {
			int[] floorList = this.call.getFloorList();
			if (floorList == null) {
				return;
			}
			int i = 0;
			for (i = 0; i < floorList.length && i - 1 < this.videoThumbnails.length; i++) {
				Contact contact = this.call.getContact(floorList[i]);
				if (contact == null) {
					continue;
				}
				VideoInFrame frame = i == 0 ? this.videoMaster : this.videoThumbnails[i - 1];
				TextView label = i == 0 ? this.labelMaster : this.labelThumbnails[i - 1];

				if (frame != null) {
					frame.setVisibility(contact.hasVideo() ? View.VISIBLE : View.GONE);
					contact.setView(frame);
				}
				if (label != null) {
					label.setText(contact.getDisplayName());
					label.setVisibility(isFloating ? View.GONE : View.VISIBLE);
				}
			}
			i = i == 0 ? 1 : i;
			for (; i - 1 < this.videoThumbnails.length; i++) {
				VideoInFrame frame = this.videoThumbnails[i - 1];
				TextView label = this.labelThumbnails[i - 1];

				if (frame != null) {
					frame.setVisibility(View.GONE);
				}
				if (label != null) {
					label.setText(null);
					label.setVisibility(isFloating ? View.GONE : View.VISIBLE);
				}
			}
		} else {
			Contact contact = this.call.getContact(Contact.DEFAULT_CONTACT_ID);
			if (contact != null) {
				contact.setView(this.videoMaster);
			}
			if (this.videoMaster != null) {
				this.videoMaster.setVisibility(View.VISIBLE);
			}
			if (this.labelMaster != null) {
				this.labelMaster.setText(contact == null ? null : contact.getDisplayName());
				this.labelMaster.setVisibility(isFloating ? View.GONE : View.VISIBLE);
			}

		}
	}

	/**
	 * Helper method to update the state of the {@link CallControls}
	 */
	private void updateCallControls() {
		if (this.controls != null) {
			this.controls.updateCallControls(this.call, this.floatingWindowController.isFloating());
		}
	}

	/**
	 * Updates both thumbnails and PiP {@link #videoSelf}
	 */
	private void updateViewSizes() {
		if (this.call == null || this.call.getStatus() == CallStatus.ENDED) {
			return;
		}

		boolean isFloating = this.floatingWindowController.isFloating();

		updateThumbnails(isFloating);
		updatePip(isFloating);
	}

	/**
	 * Updates the PiP {@link #videoSelf} according to the floating window state
	 * 
	 * @param isFloating
	 */
	private void updatePip(boolean isFloating) {
		boolean isReceivingVideo = this.call.isReceivingVideo();
		boolean isReceivingScreenShare = this.call.isReceivingScreenShare();

		android.widget.FrameLayout.LayoutParams pipLayoutParams = (android.widget.FrameLayout.LayoutParams) this.videoSelf.getLayoutParams();

		boolean condition = isReceivingVideo | isReceivingScreenShare;

		final int pipMargin = condition ? (!isFloating ? this.dimenVideoSelfMarginFullscreen : this.dimenVideoSelfMargin) : 0;
		final int pipWidth = condition ? (!isFloating ? this.dimenVideoSelfSizeFullscreen : this.dimenVideoSelfSize) : ViewGroup.LayoutParams.MATCH_PARENT;
		final int pipHeight = condition ? (!isFloating ? this.dimenVideoSelfSizeFullscreen : this.dimenVideoSelfSize) : ViewGroup.LayoutParams.MATCH_PARENT;
		final int pipGravity = condition ? Gravity.BOTTOM | Gravity.RIGHT : Gravity.CENTER;

		pipLayoutParams.width = pipWidth;
		pipLayoutParams.height = pipHeight;
		pipLayoutParams.setMargins(pipMargin, pipMargin, pipMargin, pipMargin);
		pipLayoutParams.gravity = pipGravity;

		this.videoSelf.setLayoutParams(pipLayoutParams);
	}

	/**
	 * Flag the {@link CallViewController} as paused.<br />
	 * Can be used along with configuration changes.
	 */
	public void pause() {
		this.paused = true;
	}

	/**
	 * @return <code>true</code> if the {@link CallViewController} was previously paused, <code>false</code> otherwise
	 */
	public boolean isPaused() {
		return this.paused;
	}

	/**
	 * Flag the {@link CallViewController} as resumed
	 */
	public void resume() {
		this.paused = false;
	}
}
