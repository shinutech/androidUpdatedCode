package net.rtccloud.helper.fragment;

import com.synsormed.mobile.R;

import net.rtccloud.helper.App;
//import net.rtccloud.helper.R;
import net.rtccloud.helper.view.ContactAdapter;
import net.rtccloud.helper.view.ContactAdapter.OnContactClickListener;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.FloorListEvent;
import net.rtccloud.sdk.event.call.ParticipantEvent;
import net.rtccloud.sdk.event.call.ParticipantListEvent;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * The {@link ConferencePanelFragment} is used to display the list of {@link Contact}s available in a conference call and actions associated to this conference such as:
 * <ul>
 * <li>Mute a contact {@link #onMute(Contact)}</li>
 * <li>Deafen a contact {@link #onDeafen(Contact)}</li>
 * <li>Handup a contact {@link #onHandUp(Contact)}</li>
 * <li>Kick a contact {@link #onKick(Contact)}</li>
 * <li>Mute all contacts {@link #onMuteAll()}</li>
 * <li>Deafen all contacts {@link #onDeafenAll()}</li>
 * </ul>
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 * 
 */
public class ConferencePanelFragment extends Fragment implements OnContactClickListener, OnClickListener {

	/** Tag to identify the {@link ConferencePanelFragment} */
	public static final String TAG = ConferencePanelFragment.class.getSimpleName();

	/** The ListView to display contacts */
	private ListView mListView;
	/** Manage the contacts to display */
	private ContactAdapter mContactAdapter;
	/** Button bar containing admin controls */
	private LinearLayout mAdminControls;
	/** Admin button to mute all contacts */
	private Button mMuteAll;
	/** Admin button to deafen all contacts */
	private Button mDeafenAll;

	/** The conference call associated with this Fragment */
	protected Call mCall;

	/**
	 * Helper method to show a {@link ConferencePanelFragment}
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use
	 */
	public static void show(FragmentManager fm) {
		fm.beginTransaction().add(R.id.conference_panel_container, ConferencePanelFragment.newInstance(), ConferencePanelFragment.TAG).commit();
	}

	/**
	 * Helper method to hide the {@link ConferencePanelFragment}
	 * 
	 * @param fm
	 *            The {@link FragmentManager} to use
	 */
	public static void hide(FragmentManager fm) {
		Fragment fragment = fm.findFragmentByTag(ConferencePanelFragment.TAG);
		if (fragment != null) {
			fm.beginTransaction().remove(fragment).commit();
		}
	}

	/**
	 * @return a new instance of {@link ConferencePanelFragment}
	 */
	public static ConferencePanelFragment newInstance() {
		return new ConferencePanelFragment();
	}

	/**
	 * Required empty public constructor
	 */
	public ConferencePanelFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (Rtcc.instance() == null) {
			return;
		}
		this.mCall = Rtcc.instance().getCurrentCall();
		if (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED || !this.mCall.isConference()) {
			getActivity().getFragmentManager().beginTransaction().remove(this).commit();
			return;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.mCall == null || this.mCall.getStatus() == CallStatus.ENDED || !this.mCall.isConference()) {
			getActivity().getFragmentManager().beginTransaction().remove(this).commit();
			return null;
		}

		View view = inflater.inflate(R.layout.fragment_conference_panel, container, false);
		this.mAdminControls = (LinearLayout) view.findViewById(R.id.row_item_admin_controls);
		Contact myself = this.mCall.getMyself();
		this.mAdminControls.setVisibility(myself != null && myself.isAdmin() ? View.VISIBLE : View.GONE);
		this.mListView = (ListView) view.findViewById(R.id.listView);
		if (this.mContactAdapter == null) {
			this.mContactAdapter = new ContactAdapter(getActivity(), this.mCall.getMyself(), this.mCall.getContacts(), this);
		}
		this.mListView.setAdapter(this.mContactAdapter);

		this.mDeafenAll = (Button) view.findViewById(R.id.row_item_deafen_all);
		this.mMuteAll = (Button) view.findViewById(R.id.row_item_mute_all);
		this.mDeafenAll.setOnClickListener(this);
		this.mMuteAll.setOnClickListener(this);

		updateAdapter();
		return view;
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
	public void onDestroy() {
		super.onDestroy();
		if (this.mContactAdapter != null) {
			this.mContactAdapter.clear();
			this.mContactAdapter = null;
		}
		if (this.mListView != null) {
			this.mListView.setAdapter(null);
			this.mListView = null;
		}
	}

	/**
	 * Update the {@link #mContactAdapter} internal data
	 */
	private void updateAdapter() {
		Contact myself = this.mCall.getMyself();
		this.mAdminControls.setVisibility(myself != null && myself.isAdmin() ? View.VISIBLE : View.GONE);

		if (this.mContactAdapter != null) {
			this.mContactAdapter.update(this.mCall.getContacts());
		}
	}

	/**
	 * Update the Admin controls activated state and text
	 */
	private void updateAdminControls() {
		this.mDeafenAll.setActivated(this.mCall.isAllDeafen());
		this.mMuteAll.setActivated(this.mCall.isAllMute());

		this.mDeafenAll.setText(this.mCall.isAllDeafen() ? R.string.conf_control_undeafen_all : R.string.conf_control_deafen_all);
		this.mMuteAll.setText(this.mCall.isAllMute() ? R.string.conf_control_unmute_all : R.string.conf_control_mute_all);
	}

	@Override
	public void onKick(Contact contact) {
		App.breadcrumb("Contact.kick() {%s}", String.valueOf(contact.getId()));
		contact.kick();
	}

	@Override
	public void onHandUp(Contact contact) {
		App.breadcrumb("Contact.handUp() {%s}", String.valueOf(contact.getId()));
		boolean handUp = !contact.isHandUp();
		contact.handUp(handUp);
		/* Un-mute and Un-deafen the contact */
		if (this.mCall.getMyself().isAdmin() && !handUp) {
			if (contact.isDeafen()) {
				contact.deafen(false);
			}
			if (contact.isMute()) {
				contact.mute(false);
			}
		}
	}

	@Override
	public void onDeafen(Contact contact) {
		App.breadcrumb("Contact.deafen() {%s}", String.valueOf(contact.getId()));
		contact.deafen(!contact.isDeafen());
	}

	@Override
	public void onMute(Contact contact) {
		App.breadcrumb("Contact.mute() {%s}", String.valueOf(contact.getId()));
		contact.mute(!contact.isMute());
	}

	@Override
	public void onMuteAll() {
		App.breadcrumb("Call.muteAll()");
		this.mCall.muteAll(this.mCall.isAllMute());
	}

	@Override
	public void onDeafenAll() {
		App.breadcrumb("Call.deafenAll()");
		this.mCall.deafenAll(this.mCall.isAllDeafen());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.row_item_deafen_all:
			App.breadcrumb("Call.deafenAll()");
			ConferencePanelFragment.this.mCall.deafenAll(!ConferencePanelFragment.this.mCall.isAllDeafen());
			break;
		case R.id.row_item_mute_all:
			App.breadcrumb("Call.muteAll()");
			ConferencePanelFragment.this.mCall.muteAll(!ConferencePanelFragment.this.mCall.isAllMute());
			break;

		default:
			break;
		}
	}

	/**
	 * This method catches all {@link ParticipantEvent}
	 * 
	 * @param event
	 *            The delivered {@link ParticipantEvent}
	 */
	@RtccEventListener
	public void onCallConferenceParticipantChangedEvent(ParticipantEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		updateAdapter();
		updateAdminControls();
	}

	/**
	 * This method catches all {@link ParticipantListEvent}
	 * 
	 * @param event
	 *            The delivered {@link ParticipantListEvent}
	 */
	@RtccEventListener
	public void onCallConferenceParticipantListEvent(ParticipantListEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		updateAdapter();
		updateAdminControls();
	}

	/**
	 * This method catches all {@link FloorListEvent}
	 * 
	 * @param event
	 *            The delivered {@link FloorListEvent}
	 */
	@RtccEventListener
	public void onCallConferenceFloorListEvent(FloorListEvent event) {
		if (!event.isMatching(this.mCall)) {
			return;
		}

		updateAdapter();
		updateAdminControls();
	}
}
