package org.matrix.client.android;

import android.content.Context;

import android.database.DataSetObserver;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MemberListAdapter extends ArrayAdapter<Model.User> {
	private final LayoutInflater inflater;
	private final Context ctx;

	public MemberListAdapter(
		final Context ctx
	) {
		super(ctx, 0, new ArrayList<Model.User>());
		this.ctx = ctx;
		inflater = LayoutInflater.from(ctx);
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
		final Model.User member = getItem(position);

		View view = convertView;
		if (view == null) {
			view = (View) inflater.inflate(R.layout.member_list_item, null);
		}

		final TextView name_view = (TextView) view.findViewById(R.id.user_name);
		name_view.setText(member.user_id);
		final TextView tvCount = (TextView) view.findViewById(R.id.last_seen);
		tvCount.setText("18s ago");
		final ImageView avatar = (ImageView) view.findViewById(R.id.user_avatar);
		if(member.avatar == null) {
			Log.i("Member", "No avatar for " + member.user_id);
			avatar.setImageResource(R.drawable.default_profile);
		} else {
			Picasso
				.with(ctx)
				.load(
					member.avatar
				)
				.placeholder(R.drawable.default_profile)
				.error(R.drawable.default_profile)
				.transform(new CropSquareTransformation())
				.into(avatar);
		}
		return view;
	}
}

