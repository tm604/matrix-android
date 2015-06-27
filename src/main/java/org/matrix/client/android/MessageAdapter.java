package org.matrix.client.android;

import android.app.Activity;

import android.content.Context;

import android.os.AsyncTask;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Message types currently defined:
 * <ul>
 * <li>text - this is a plain text message, we go as far as making URLs clickable but that's about it
 * <li>image - these are messages that contain a single image, actual image would be retrieved from the
 * thumbnail with the ability to share, download or view the full image
 * <li>geo - geolocation isn't supported anywhere else, but we can show a little map or something
 * <li>code - program code...?
 * </ul>
 *
 * Each message also has a few common attributes:
 * <ul>
 * <li>From me - messages that we send are displayed RTL to make them stand out a bit
 * <li>Pending - when we send a message, it'll probably take a while before it comes back via
 * the event polling thread. We need some way of indicating that we've sent a message, so we
 * put these in the listview anyway, and remove them when the event_id comes back.
 * </ul>
 */
public class MessageAdapter extends ArrayAdapter<Model.Room.Message> {
	private final Activity ctx;
	private ListView list = null;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	static class ViewHolder {
		public TextView content;
		public TextView name;
		public TextView date;
		public TextView time;
		public ImageView avatar;
	}

	public MessageAdapter(final Activity context) {
		super(context, R.layout.text_message);
		/*
		{
			final TextMessage tm = new TextMessage();
			tm.msgtype = "m.text";
			tm.body = "a message body";
			messages.add(tm);
		}
		{
			final TextMessage tm = new TextMessage();
			tm.msgtype = "m.text";
			tm.body = "this is another, longer message. it may contain some text or even links like http://matrix.org orhttp://www.google.com and this is likely to go on for several lines, depending on font size and any other considerations that may affect the placement and rendering of this widget, such as the XML layout definition or the code responsible tor inflating the layout and maintaining it once this has been done.";
			messages.add(tm);
		}
		*/
		this.ctx = context;
	}

	@Override
	public int getItemViewType(int position) {
		return 0; // position % 2;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = ctx.getLayoutInflater();
			view = inflater.inflate((getItemViewType(position) == 0) ? R.layout.text_message : R.layout.text_message_mine, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) view.findViewById(R.id.user_name);
			viewHolder.date = (TextView) view.findViewById(R.id.post_date);
			viewHolder.time = (TextView) view.findViewById(R.id.post_time);
			viewHolder.content = (TextView) view.findViewById(R.id.post_content);
			viewHolder.avatar = (ImageView) view.findViewById(R.id.user_avatar);
			view.setTag(viewHolder);
		}

		final ViewHolder holder = (ViewHolder) view.getTag();
		final Model.Room.Message msg = getItem(position);
		// if(msg.isText()) {
			holder.content.setText(msg.body);
		// } else {
		// 	holder.content.setText("not a text message, placeholder content");
		// }

		final Date posted = new Date(msg.ts);
	    final String date = dateFormat.format(posted);
		holder.date.setText(date);
	    final String time = timeFormat.format(posted).toLowerCase();
		holder.time.setText(time);

		if(msg.user == null) {
			Log.e("Message", "No user for this message?");
			holder.avatar.setImageResource(R.drawable.default_profile);
		} else {
			if(msg.user.displayname == null) {
				holder.name.setText(msg.user.user_id);
			} else {
				holder.name.setText(msg.user.displayname);
			}
			if(msg.user.avatar == null) {
				Log.d("Message", "Default profile image");
				holder.avatar.setImageResource(R.drawable.default_profile);
			} else {
				Picasso
					.with(ctx)
					.load(
						msg.user.avatar
					)
					.placeholder(R.drawable.default_profile)
					.error(R.drawable.default_profile)
					.transform(new CropSquareTransformation())
					.into(holder.avatar);
			}
		}
		return view;
	}
}

