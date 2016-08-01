package net.rtccloud.helper.fragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.synsormed.mobile.R;

import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.sdk.MeetingPoint;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

/**
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class CreateMeetingPointDialogFragment extends DialogFragment {

	/** Start time */
	protected Time mStartTime;
	/** End time */
	protected Time mEndTime;

	/**
	 * Factory method. Creates a new instance of this fragment.
	 * @return A new instance of {@link CreateMeetingPointDialogFragment}.
	 */
	public static CreateMeetingPointDialogFragment newInstance() {
		return new CreateMeetingPointDialogFragment();
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Register the fragment as an event listener */
		Rtcc.eventBus().register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		/* Unregister the fragment */
		Rtcc.eventBus().unregister(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initStartTime();
		initEndTime();

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_meetingpoint, null);
		final EditText title = (EditText) view.findViewById(R.id.mp_title);
		final EditText location = (EditText) view.findViewById(R.id.mp_location);
		final EditText startTime = (EditText) view.findViewById(R.id.mp_start_time);
		final EditText endTime = (EditText) view.findViewById(R.id.mp_end_time);
		final Switch type = (Switch) view.findViewById(R.id.mp_type);

		validateTime();
		updateTime(startTime, this.mStartTime);
		updateTime(endTime, this.mEndTime);

		startTime.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					pickStartDate(startTime);
				}
				return true;
			}
		});

		endTime.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					pickEndDate(endTime);
				}
				return true;
			}
		});

		type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((View) startTime.getParent()).setVisibility(isChecked ? View.VISIBLE : View.GONE);
				((View) endTime.getParent()).setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});

		AlertDialog.Builder builder = new Builder(getActivity()).setTitle(R.string.action_create_mp).setView(view).setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((AuthenticatedFragment) getTargetFragment()).onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_mp_creating), true);

				MeetingPoint.Builder mp = new MeetingPoint.Builder(type.isChecked() ? MeetingPoint.Type.SCHEDULED : MeetingPoint.Type.PERMANENT);
				mp.title(title.getText().toString()).location(location.getText().toString());
				if (type.isChecked()) {
					try {
						mp.startTime(((Integer) startTime.getTag()).intValue()).endTime(((Integer) endTime.getTag()).intValue());
					} catch (NumberFormatException e) {
						Log.e(CreateMeetingPointDialogFragment.class.getSimpleName(), "Wrong number format", e);
					}
				}
				App.breadcrumb("RtccEngine.createMeetingPoint(%s)", mp.toString());
				Rtcc.instance().createMeetingPoint(mp);
				dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * Initialize the {@link #mStartTime} if it's not already
	 */
	protected void initStartTime() {
		if (this.mStartTime == null) {
			final Calendar cal = Calendar.getInstance();
			this.mStartTime = new Time();
			this.mStartTime.set(0, cal.get(Calendar.MINUTE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
		}
	}

	/**
	 * Display a {@link DatePickerDialog} to set the start date
	 * 
	 * @param editStartTime
	 */
	protected void pickStartDate(final EditText editStartTime) {
		final DatePickerDialog picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			/* workaround for onDateSetCalled twice */
			boolean flag = false;

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				if (this.flag)
					return;
				this.flag = true;

				CreateMeetingPointDialogFragment.this.mStartTime.set(0, CreateMeetingPointDialogFragment.this.mStartTime.minute, CreateMeetingPointDialogFragment.this.mStartTime.hour, dayOfMonth, monthOfYear, year);
				pickStartTime(editStartTime);
			}
		}, this.mStartTime.year, this.mStartTime.month, this.mStartTime.monthDay);
		picker.setCancelable(false);
		picker.setCanceledOnTouchOutside(false);
		picker.show();
	}

	/**
	 * Display a {@link DatePickerDialog} to set the start time
	 * 
	 * @param editStartTime
	 */
	protected void pickStartTime(final EditText editStartTime) {
		TimePickerDialog picker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				CreateMeetingPointDialogFragment.this.mStartTime.set(0, minute, hourOfDay, CreateMeetingPointDialogFragment.this.mStartTime.monthDay, CreateMeetingPointDialogFragment.this.mStartTime.month, CreateMeetingPointDialogFragment.this.mStartTime.year);
				if (CreateMeetingPointDialogFragment.this.mEndTime.toMillis(true) < CreateMeetingPointDialogFragment.this.mStartTime.toMillis(true)) {
					CreateMeetingPointDialogFragment.this.mEndTime.set(CreateMeetingPointDialogFragment.this.mStartTime);
				}
				validateTime();
				updateTime(editStartTime, CreateMeetingPointDialogFragment.this.mStartTime);
			}
		}, this.mStartTime.hour, this.mStartTime.minute, false);
		picker.setCancelable(false);
		picker.setCanceledOnTouchOutside(false);
		picker.show();
	}

	/**
	 * Initialize the {@link #mEndTime} if it's not already
	 */
	protected void initEndTime() {
		if (this.mEndTime == null) {
			final Calendar cal = Calendar.getInstance();
			this.mEndTime = new Time();
			this.mEndTime.set(0, cal.get(Calendar.MINUTE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
		}
	}

	/**
	 * Display a {@link DatePickerDialog} to set the end date
	 * 
	 * @param editEndTime
	 */
	protected void pickEndDate(final EditText editEndTime) {
		DatePickerDialog picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			/* workaround for onDateSetCalled twice */
			boolean flag = false;

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				if (this.flag)
					return;
				this.flag = true;

				CreateMeetingPointDialogFragment.this.mEndTime.set(0, CreateMeetingPointDialogFragment.this.mStartTime.minute, CreateMeetingPointDialogFragment.this.mStartTime.hour, dayOfMonth, monthOfYear, year);
				pickEndTime(editEndTime);
			}
		}, this.mEndTime.year, this.mEndTime.month, this.mEndTime.monthDay);
		picker.setCancelable(false);
		picker.setCanceledOnTouchOutside(false);
		picker.show();
	}

	/**
	 * Display a {@link DatePickerDialog} to set the end time
	 * 
	 * @param editEndTime
	 */
	protected void pickEndTime(final EditText editEndTime) {
		TimePickerDialog picker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				CreateMeetingPointDialogFragment.this.mEndTime.set(0, minute, hourOfDay, CreateMeetingPointDialogFragment.this.mEndTime.monthDay, CreateMeetingPointDialogFragment.this.mStartTime.month, CreateMeetingPointDialogFragment.this.mEndTime.year);
				validateTime();
				updateTime(editEndTime, CreateMeetingPointDialogFragment.this.mEndTime);
			}
		}, this.mEndTime.hour, this.mEndTime.minute, false);
		picker.setCancelable(false);
		picker.setCanceledOnTouchOutside(false);
		picker.show();
	}

	/**
	 * Rewrite the end time if it's before the start time
	 */
	protected void validateTime() {
		if (CreateMeetingPointDialogFragment.this.mEndTime.before(this.mStartTime)) {
			CreateMeetingPointDialogFragment.this.mEndTime.set(CreateMeetingPointDialogFragment.this.mStartTime.toMillis(true));
		}
	}

	/**
	 * Display the corresponding time inside the provided EditText
	 * 
	 * @param edit
	 * @param time
	 */
	@SuppressWarnings("boxing")
	protected void updateTime(EditText edit, Time time) {
		long ms = time.toMillis(true);
		edit.setText(DateFormat.getDateTimeInstance().format(new Date(ms)));
		edit.setTag((int) (ms / 1000));
	}

	/**
	 * This method catches all {@link AuthenticatedEvent}.
	 * <ul>
	 * <li><b>It must</b> be annotated with @{@link RtccEventListener}</li>
	 * <li><b>It must</b> take one argument which type is {@link AuthenticatedEvent}</li>
	 * </ul>
	 * 
	 * @param event
	 *            The delivered {@link AuthenticatedEvent}
	 */
	@RtccEventListener
	public void onAuthenticatedEvent(final AuthenticatedEvent event) {
		if (!event.isSuccess()) {
			dismiss();
		}
	}
}
