package org.matrix.client.android;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class RoomListItem {
	public final String name;
	public final int count;

	public RoomListItem(final String name, final int count) {
		this.name = name;
		this.count = count;
	}

	public int getViewType() { return 0; }

	public View getView(
		LayoutInflater inflater,
		View convertView
	) {
		View view = convertView;
		if (view == null) {
			view = (View) inflater.inflate(R.layout.room_list_item, null);
		}

		final TextView name_view = (TextView) view.findViewById(R.id.room_name);
		name_view.setText(name);
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

