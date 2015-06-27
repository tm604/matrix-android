package org.matrix.client.android;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MemberListItem {
	public final String name;
	public final Long lastSeen;

	public MemberListItem(final String name, final Long lastSeen) {
		this.name = name;
		this.lastSeen = lastSeen;
	}

	public int getViewType() { return 0; }

	public View getView(
		LayoutInflater inflater,
		View convertView
	) {
		View view = convertView;
		if (view == null) {
			view = (View) inflater.inflate(R.layout.member_list_item, null);
		}

		final TextView name_view = (TextView) view.findViewById(R.id.user_name);
		name_view.setText(name);
		final TextView tvCount = (TextView) view.findViewById(R.id.last_seen);
		tvCount.setText("18s ago");
		return view;
	}
}

