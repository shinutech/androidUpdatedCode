package net.rtccloud.helper.view;

import com.synsormed.mobile.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

//import net.rtccloud.helper.R;
import net.rtccloud.helper.listener.OnCallControlClickListener;
import net.rtccloud.helper.util.CheatSheet;
import net.rtccloud.helper.util.Ui;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.AudioRoute;

/**
 * ViewGroup displaying call controls. To be notified of click event, simply register a {@link OnCallControlClickListener}. {@link CallButton}'c color can be set in xml with the
 * <code>app:btn_color</code> attribute.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
@SuppressLint("NewApi") public class CallControls extends LinearLayout implements OnClickListener {

	/** Value used to color assets at runtime */
	protected int btnColor = -1;

	protected ImageButton patientInfo;
	/** The speakers button */
	protected ImageButton speakers;
	/** The micro button */
	protected ImageButton micro;
	/** The video button */
	protected ImageButton video;
	/** The camera switch button */
	protected ImageButton camera;
	/** The fullscreen button */
	protected ImageButton fullscreen;
	/** The scale type button */
	protected ImageButton scale;
	/** The share button */
	protected ImageButton share;
	/** The hangup button */
	protected ImageButton hangup;

	/**
	 * Enum of the controls available in the {@link CallControls}.
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public enum CallButton {
		PATIENT_INFO,
		/** SPEAKERS */
		SPEAKERS,
		/** MICRO */
		MICRO,
		/** VIDEO */
		VIDEO,
		/** SWITCH */
		SWITCH,
		/** FULLSCREEN */
		FULLSCREEN,
		/** SCALE */
		SCALE,
		/** SHARE */
		SHARE,
		/** HANGUP */
		HANGUP,
		/** UNKNOWN */
		UNKNOWN
	}

	/** Callback to report button clicks */
	protected OnCallControlClickListener mCallControlsListener;

	/**
	 * @see View#View(Context, AttributeSet, int)
	 */
	public CallControls(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	/**
	 * @see View#View(Context, AttributeSet)
	 */
	public CallControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	/**
	 * @see View#View(Context)
	 */
	public CallControls(Context context) {
		super(context);
		init(null);
	}

	/**
	 * Initialize the views inside the {@link CallControls}.
	 * 
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {
		/* Fetch the xml defined color if any */
		if (attrs != null) {
			TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CallControls, 0, 0);
			try {
				this.btnColor = a.getColor(R.styleable.CallControls_btn_color, -1);
			} finally {
				a.recycle();
			}
		}

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.call_controls, this);
		this.patientInfo= (ImageButton) findViewById(R.id.call_control_patient_info);
		this.speakers = (ImageButton) findViewById(R.id.call_control_speakers);
		this.micro = (ImageButton) findViewById(R.id.call_control_micro);
		this.video = (ImageButton) findViewById(R.id.call_control_video);
		this.camera = (ImageButton) findViewById(R.id.call_control_camera);
		this.fullscreen = (ImageButton) findViewById(R.id.call_control_fullscreen);
		this.scale = (ImageButton) findViewById(R.id.call_control_scale_type);
		this.share = (ImageButton) findViewById(R.id.call_control_share);
		this.hangup = (ImageButton) findViewById(R.id.call_control_hangup);

		if(isInEditMode()) {
			return;
		}
		
		/* Color assets at runtime if a custom color was defined */
		if (this.btnColor != -1) {
			Context ctx = getContext();
			this.patientInfo.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_patient_info, this.btnColor));
			this.speakers.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_speakers, this.btnColor));
			this.micro.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_micro, this.btnColor));
			this.video.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_video, this.btnColor));
			this.camera.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_camera, this.btnColor));
			this.fullscreen.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_fullscreen, this.btnColor));
			this.scale.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_scale, this.btnColor));
			this.share.setImageDrawable(Ui.getColoredDrawable(ctx, R.drawable.ic_call_controls_share, this.btnColor));
		} else {
			this.patientInfo.setImageResource(R.drawable.ic_call_controls_patient_info);
			this.speakers.setImageResource(R.drawable.ic_call_controls_speakers);
			this.micro.setImageResource(R.drawable.ic_call_controls_micro);
			this.video.setImageResource(R.drawable.ic_call_controls_video);
			this.camera.setImageResource(R.drawable.ic_call_controls_camera);
			this.fullscreen.setImageResource(R.drawable.ic_call_controls_fullscreen);
			this.scale.setImageResource(R.drawable.ic_call_controls_scale);
			this.share.setImageResource(R.drawable.ic_call_controls_share);
		}
		this.hangup.setImageResource(R.drawable.ic_call_controls_hangup);

		CheatSheet.setup(this.patientInfo, this.speakers, this.micro, this.video, this.camera, this.fullscreen, this.share, this.scale, this.hangup);
		this.patientInfo.setOnClickListener(this);
		this.speakers.setOnClickListener(this);
		this.micro.setOnClickListener(this);
		this.video.setOnClickListener(this);
		this.camera.setOnClickListener(this);
		this.fullscreen.setOnClickListener(this);
		this.scale.setOnClickListener(this);
		this.share.setOnClickListener(this);
		this.hangup.setOnClickListener(this);

		this.camera.setVisibility(Camera.getNumberOfCameras() < 2 ? View.GONE : View.VISIBLE);
		
		updateCallControls(null, true);
		Log.d("gunjot", "Initialize the view successfully in Call Controls");
	}

	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	/**
	 * Update the states of the {@link CallControls} with the provided call.
	 * 
	 * @param call
	 *            The {@link Call} state to reflect
	 * @param floatingWindow
	 *            If the view is in a floatingWindow
	 */
	public void updateCallControls(Call call, boolean floatingWindow) {
		if (call != null) {
			if (getVisibility() == View.GONE) {
				setVisibility(View.VISIBLE);
			}
			this.camera.setVisibility(call.isSendingVideo() && Camera.getNumberOfCameras() > 1 ? View.VISIBLE : View.GONE);
			this.video.setActivated(!call.isSendingVideo());
			this.micro.setActivated(!call.isSendingAudio());
			//Commenting out unused buttons 8/9/15 aholmes
            //this.fullscreen.setActivated(!floatingWindow);
			//this.share.setVisibility(call.isWebRTC() || call.isReceivingScreenShare() ? View.GONE : View.VISIBLE);
			//this.share.setActivated(call.isSendingScreenShare());
            this.share.setVisibility(View.GONE);
			this.speakers.setActivated(call.getAudioRoute() == AudioRoute.SPEAKER);
		}
		Log.d("gunjot", "updated the controls in Call Controls");
	}

	/**
	 * Register a callback to be invoked when a {@link CallButton} is clicked.
	 * 
	 * @param listener
	 *            The callback that will be called
	 */
	public void setOnCallControlsListener(OnCallControlClickListener listener) {
		this.mCallControlsListener = listener;
	}

	@Override
	public void onClick(View v) {
		CallButton button = CallButton.UNKNOWN;
		switch (v.getId()) 
		{
		case R.id.call_control_patient_info:
			Log.d("gunjot", "clicked in main view");
			button=CallButton.PATIENT_INFO;
			break;
		case R.id.call_control_speakers:
			button = CallButton.SPEAKERS;
			break;
		case R.id.call_control_micro:
			button = CallButton.MICRO;
			break;
		case R.id.call_control_video:
			button = CallButton.VIDEO;
			break;
		case R.id.call_control_camera:
			button = CallButton.SWITCH;
			break;
		case R.id.call_control_hangup:
			button = CallButton.HANGUP;
			break;
		case R.id.call_control_fullscreen:
			button = CallButton.FULLSCREEN;
			break;
		case R.id.call_control_scale_type:
			button = CallButton.SCALE;
			break;
		case R.id.call_control_share:
			button = CallButton.SHARE;
			break;
		default:
			throw new UnsupportedOperationException("Unknown button");
		}

		if (this.mCallControlsListener != null) {
			this.mCallControlsListener.onCallControlsClick(button);
		}
	}

	/**
	 * Class used to store {@link CallControls}'s state
	 */
	static class SavedState extends BaseSavedState {

		/**
		 * @see android.view.View.BaseSavedState#BaseSavedState(Parcelable)
		 */
		public SavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Whether or not buttons are activated :
		 * <ul>
		 * <li>[0] = item_camera</li>
		 * <li>[1] = item_video</li>
		 * <li>[2] = item_speakers</li>
		 * <li>[3] = item_micro</li>
		 * <li>[4] = item_fullscreen</li>
		 * <li>[5] = item_scale</li>
		 * <li>[6] = item_share</li>
		 * <li>[7] = item_hangup</li>
		 * </ul>
		 */
		boolean[] activated = {};

		/**
		 * Whether or not buttons are visible :
		 * <ul>
		 * <li>[0] = item_camera</li>
		 * <li>[1] = item_video</li>
		 * <li>[2] = item_speakers</li>
		 * <li>[3] = item_micro</li>
		 * <li>[4] = item_fullscreen</li>
		 * <li>[5] = item_scale</li>
		 * <li>[6] = item_share</li>
		 * <li>[7] = item_hangup</li>
		 * </ul>
		 */
		int[] visibility = {};

		/**
		 * @see android.view.View.BaseSavedState#BaseSavedState(Parcel)
		 */
		protected SavedState(Parcel in) {
			super(in);
			this.activated = new boolean[9];
			this.visibility = new int[9];
			in.readBooleanArray(this.activated);
			in.readIntArray(this.visibility);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeBooleanArray(this.activated);
			out.writeIntArray(this.visibility);
		}

		/**
		 * Required field that makes Parcelables from a Parcel
		 */
		@SuppressWarnings("hiding")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState state = new SavedState(superState);
		state.activated = new boolean[] {this.camera.isActivated(), this.video.isActivated(), this.speakers.isActivated(), this.micro.isActivated(), this.fullscreen.isActivated(), this.scale.isActivated(), this.share.isActivated(), this.hangup.isActivated(), this.patientInfo.isActivated() };
		state.visibility = new int[] { this.camera.getVisibility(), this.video.getVisibility(), this.speakers.getVisibility(), this.micro.getVisibility(), this.fullscreen.getVisibility(), this.scale.getVisibility(), this.share.getVisibility(), this.hangup.getVisibility(), this.patientInfo.getVisibility() };
		return state;
	}

	@Override
	public void onRestoreInstanceState(Parcelable parcelable) {
		if (!(parcelable instanceof SavedState)) {
			super.onRestoreInstanceState(parcelable);
			return;
		}

		SavedState state = (SavedState) parcelable;
		super.onRestoreInstanceState(state.getSuperState());
		this.camera.setActivated(state.activated[0]);
		this.video.setActivated(state.activated[1]);
		this.speakers.setActivated(state.activated[2]);
		this.micro.setActivated(state.activated[3]);
		this.fullscreen.setActivated(state.activated[4]);
		this.scale.setActivated(state.activated[5]);
		this.share.setActivated(state.activated[6]);
		this.hangup.setActivated(state.activated[7]);
		this.patientInfo.setActivated(state.activated[8]);

		this.camera.setVisibility(state.visibility[0]);
		this.video.setVisibility(state.visibility[1]);
		this.speakers.setVisibility(state.visibility[2]);
		this.micro.setVisibility(state.visibility[3]);
		this.fullscreen.setVisibility(state.visibility[4]);
		this.scale.setVisibility(state.visibility[5]);
		this.share.setVisibility(state.visibility[6]);
		this.hangup.setVisibility(state.visibility[7]);
		this.patientInfo.setVisibility(state.visibility[8]);
	}
}
