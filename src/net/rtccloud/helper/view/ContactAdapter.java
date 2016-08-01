package net.rtccloud.helper.view;

import java.util.ArrayList;

import com.synsormed.mobile.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

//import net.rtccloud.helper.R;
import net.rtccloud.sdk.Contact;

/**
 * Adapter used to wrap and display a list of {@link Contact}s.<br />
 * A {@link OnContactClickListener} can be registered to handle some pre-defined actions.
 * 
 * @author Simon Marquis <simon.marquis@sightcall.com>
 */
public class ContactAdapter extends BaseAdapter {

	/** Application context used to inflate layout */
	Context ctx;
	/** {@link Contact} corresponding to myself */
	Contact myself;
	/** The list of other {@link Contact}s */
	ArrayList<Contact> contacts;
	/** Callback to use when a button is clicked */
	OnContactClickListener listener;

	/** Click listener to handle Kick action */
	OnClickListener kickListener;
	/** Click listener to handle HandUp action */
	OnClickListener handListener;
	/** Click listener to handle Deafen action */
	OnClickListener deafenListener;
	/** Click listener to handle Mute action */
	OnClickListener muteListener;

	/**
	 * Interface definition for a callback to be invoked when a button is clicked
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	public interface OnContactClickListener {
		/**
		 * Called when the Kick button is clicked
		 * 
		 * @param contact
		 *            The corresponding contact
		 */
		void onKick(Contact contact);

		/**
		 * Called when the HandUp button is clicked
		 * 
		 * @param contact
		 *            The corresponding contact
		 */
		void onHandUp(Contact contact);

		/**
		 * Called when the Deafen button is clicked
		 * 
		 * @param contact
		 *            The corresponding contact
		 */
		void onDeafen(Contact contact);

		/**
		 * Called when the Mute button is clicked
		 * 
		 * @param contact
		 *            The corresponding contact
		 */
		void onMute(Contact contact);

		/**
		 * Called when the MuteAll button is clicked
		 */
		void onMuteAll();

		/**
		 * Called when the DeafenAll button is clicked
		 */
		void onDeafenAll();

	}

	/**
	 * Custom constructor
	 * 
	 * @param ctx
	 *            The context to use
	 * @param myself
	 *            The contact corresponding to myself
	 * @param cts
	 *            The list of contacts
	 * @param l
	 *            The callback to handle button click
	 */
	public ContactAdapter(Context ctx, Contact myself, ArrayList<Contact> cts, OnContactClickListener l) {
		this.ctx = ctx.getApplicationContext();
		this.myself = myself;
		this.contacts = cts;
		this.listener = l;
		this.muteListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Contact c = (Contact) v.getTag();
				if (ContactAdapter.this.listener != null && c != null) {
					if (c.isMyself()) {
						ContactAdapter.this.listener.onMuteAll();
					} else {
						ContactAdapter.this.listener.onMute(c);
					}
				}
			}
		};
		this.deafenListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Contact c = (Contact) v.getTag();
				if (ContactAdapter.this.listener != null && c != null) {
					if (c.isMyself()) {
						ContactAdapter.this.listener.onDeafenAll();
					} else {
						ContactAdapter.this.listener.onDeafen(c);
					}
				}
			}
		};
		this.kickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Contact c = (Contact) v.getTag();
				if (ContactAdapter.this.listener != null && c != null) {
					ContactAdapter.this.listener.onKick(c);
				}
			}
		};
		this.handListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Contact c = (Contact) v.getTag();
				if (ContactAdapter.this.listener != null && c != null) {
					ContactAdapter.this.listener.onHandUp(c);
				}
			}
		};
	}

	/**
	 * Clear the list of contacts
	 */
	public void clear() {
		if (this.contacts != null) {
			this.contacts.clear();
		}
		this.contacts = null;
	}

	/**
	 * Update the contact list to display
	 * 
	 * @param cts
	 *            the new contact list
	 */
	public void update(ArrayList<Contact> cts) {
		this.contacts = cts;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return 1 + this.contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return position == 0 ? this.myself : this.contacts.get(position - 1);
	}

	@Override
	public long getItemId(int position) {
		return (position == 0 ? this.myself : this.contacts.get(position - 1)).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		View result;
		Contact contact = position == 0 ? this.myself : this.contacts.get(position - 1);
		if (convertView == null) {
			result = LayoutInflater.from(this.ctx).inflate(R.layout.row_contact, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) result.findViewById(R.id.row_item_name);

			viewHolder.mute = (ImageButton) result.findViewById(R.id.row_item_mute);
			viewHolder.mute.setOnClickListener(this.muteListener);

			viewHolder.deafen = (ImageButton) result.findViewById(R.id.row_item_deafen);
			viewHolder.deafen.setOnClickListener(this.deafenListener);

			viewHolder.hand = (ImageButton) result.findViewById(R.id.row_item_hand);
			viewHolder.hand.setOnClickListener(this.handListener);

			viewHolder.kick = (ImageButton) result.findViewById(R.id.row_item_kick);
			viewHolder.kick.setOnClickListener(this.kickListener);

			result.setTag(viewHolder);

		} else {
			result = convertView;
			viewHolder = (ViewHolder) result.getTag();
		}

		if (contact != null) {
			boolean isMyself = contact.isMyself();
			boolean isAdmin = this.myself.isAdmin();
			boolean isHandUp = contact.isHandUp();

			viewHolder.mute.setTag(contact);
			viewHolder.deafen.setTag(contact);
			viewHolder.hand.setTag(contact);
			viewHolder.kick.setTag(contact);

			viewHolder.name.setText(contact.getDisplayName());

			viewHolder.mute.setVisibility(isAdmin && isMyself ? View.GONE : View.VISIBLE);
			viewHolder.deafen.setVisibility(isAdmin && isMyself ? View.GONE : View.VISIBLE);
			viewHolder.hand.setVisibility(isAdmin && isMyself ? View.GONE : View.VISIBLE);
			viewHolder.kick.setVisibility(isAdmin && isMyself || !isAdmin ? View.GONE : View.VISIBLE);

			viewHolder.mute.setActivated(contact.isMute());
			viewHolder.deafen.setActivated(contact.isDeafen());
			viewHolder.hand.setActivated(contact.isHandUp());

			viewHolder.mute.setEnabled(isAdmin);
			viewHolder.deafen.setEnabled(isAdmin);
			viewHolder.hand.setEnabled(isAdmin && isHandUp && !isMyself || !isAdmin && isMyself);
			viewHolder.kick.setEnabled(isAdmin);

			viewHolder.mute.setAlpha(isAdmin ? 1f : 0.3f);
			viewHolder.deafen.setAlpha(isAdmin ? 1f : 0.3f);
			viewHolder.hand.setAlpha(isAdmin && isHandUp && !isMyself || !isAdmin && isMyself ? 1f : 0.3f);

			result.setBackgroundResource(isMyself ? R.color.conference_background : android.R.color.transparent);
		}

		return result;
	}

	/**
	 * ViewHolder pattern used for performance in {@link ListView}
	 * 
	 * @author Simon Marquis <simon.marquis@sightcall.com>
	 */
	static class ViewHolder {
		/** Contact displayName */
		TextView name;
		/** Mute button */
		ImageButton mute;
		/** Deafen button */
		ImageButton deafen;
		/** Handup button */
		ImageButton hand;
		/** Kick button */
		ImageButton kick;
	}
}
