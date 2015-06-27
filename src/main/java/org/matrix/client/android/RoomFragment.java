package org.matrix.client.android;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import android.util.Log;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import rx.functions.Action1;

import rx.Observer;

import rx.Scheduler;

import rx.schedulers.Schedulers;

/**
 * This handles our room layout.
 */
public class RoomFragment extends Fragment {
	private ListView listView;
	private MessageAdapter messages;
	private MemberListAdapter members;
	private String room_id;

	public RoomFragment(final String room_id) {
		super();
		this.room_id = room_id;
	}

	@Override
	public View onCreateView(
		final LayoutInflater inflater,
		final ViewGroup container,
		final Bundle savedInstanceState
	) {
		if(savedInstanceState != null) {
			Log.w("NPA", "We have a saved instance, not sure what to do");
		}
		final View root = inflater.inflate(R.layout.room, container, false);
		this.listView = (ListView) root.findViewById(R.id.room_messages);
		final View child = (View) root.findViewById(R.id.room_container);
		messages = new MessageAdapter(
			getActivity()
		);
		listView.setAdapter(messages);

		members = new MemberListAdapter(
			this.getActivity()
		);
		ViewDebug.dumpCapturedView("RoomFragment", root);
		final DrawerHandler<Model.User> dh = new DrawerHandler<>(
			this.getActivity(),
			(DrawerLayout) root.findViewById(R.id.room_layout),
			(ListView) root.findViewById(R.id.member_list),
			child,
			members,
			false
		);

		final TextView input = (TextView) root.findViewById(R.id.input_box);
		input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(final TextView v, int actionId, final KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					sendMessage(v.getText().toString());
					return true;
				}
				return false;
			}
		});
		startThings();
		return root;
	}

	public void sendMessage(final String txt) {
		Log.d("Room", "Want to send message: " + txt);
		((MainActivity) this.getActivity()).sendMessage(txt);
	}

/*
	@Override
	public Observer<Event> eventObserver() {
	}
		new Observer<Event>() {
			@Override
			public void onNext(final Event ev) {
				if(!ev.room_id.equals(this.room_id)) {
					return;
				}
				if(ev.isMessage()) {
					messagePosted(ev);
				} else if(ev.type.equals("m.room.member")) {
					presenceUpdate(ev);
				} else {
					Log.d("Room", "Unhandled event, type = " + ev.type);
				}
			}
		};
	}

	*/
	public void startThings() {
		final Model.Room room = Model.model.roomByID(room_id);
		if(room == null) {
			Log.e("Room", "we have no room by this ID");
			Model.model.rooms
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.from(UIThread.executor))
				.subscribe(new Action1<Model.Room>() {
					@Override
					public void call(final Model.Room r) {
						Log.e("Room", "we now have a room: " + r.room_id + " (looking for " + room_id + ")");
						if(r.room_id.equals(room_id)) {
							startThings();
						}
					}
				});
			return;
		}

		Log.e("Room", "Listening for messages and presence in " + room_id);
		room.messages
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.from(UIThread.executor))
			.subscribe(new Action1<Model.Room.Message>() {
				@Override
				public void call(final Model.Room.Message m) {
					Log.d("Room", "Had a message in " + room_id + " - " + m.body);
					messages.add(m);
					messages.notifyDataSetChanged();
				}
			});
		room.users
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.from(UIThread.executor))
			.subscribe(new Action1<Model.User>() {
				@Override
				public void call(final Model.User member) {
					Log.d("Room", "Had a presence update in " + room_id + " - " + member.user_id);
					members.add(member);
					members.notifyDataSetChanged();
				}
			});
	}
}

