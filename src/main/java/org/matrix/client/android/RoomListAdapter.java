package org.matrix.client.android;

import android.content.Context;

import android.database.DataSetObserver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import rx.functions.Action1;

import rx.schedulers.Schedulers;
import android.util.Log;

public class RoomListAdapter extends ArrayAdapter<Model.Room> {
	private final LayoutInflater inflater;

	public RoomListAdapter(
		final Context ctx
	) {
		super(ctx, 0, new ArrayList<Model.Room>());
		inflater = LayoutInflater.from(ctx);

		Model.model.rooms
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.from(UIThread.executor))
			.subscribe(new Action1<Model.Room>() {
				@Override
				public void call(final Model.Room r) {
					Log.d("Room", "we now have a room: " + r.room_id);
					RoomListAdapter.this.add(r);
					RoomListAdapter.this.notifyDataSetChanged();
				}
			});
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = (View) inflater.inflate(R.layout.room_list_item, null);
		}
		final Model.Room room = getItem(position);
		int count = 0;

		/* This one's never going to change */
		final TextView id_view = (TextView) view.findViewById(R.id.room_id);
		id_view.setText(room.room_id);

		/* But this one? This one might */
		final TextView name_view = (TextView) view.findViewById(R.id.room_name);
		name_view.setText(room.name);
		room.currentName
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.from(UIThread.executor))
			.subscribe(new Action1<String>() {
				@Override
				public void call(final String name) {
					Log.d("Room", "Room name changed, now " + name);
					name_view.setText(name);
				}
			});
		final TextView tvCount = (TextView) view.findViewById(R.id.message_count);
		if(count > 0) {
			tvCount.setText(String.valueOf(count));
			tvCount.setVisibility(View.VISIBLE);
		} else {
			tvCount.setVisibility(View.INVISIBLE);
		}
		return view;
	}
}

