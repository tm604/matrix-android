package org.matrix.client.android;

import android.app.ActionBar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
	private boolean bound = false;
	private MatrixService service;
	private String room_id;
	// private final String room_id = "!XqBunHwQIXUiqCaoxq:matrix.org";

	private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(
			final ComponentName className,
			final IBinder service
		) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            final MatrixService.MatrixBinding binder = (MatrixService.MatrixBinding) service;
            MainActivity.this.service = binder.getService();
            MainActivity.this.bound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            bound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		final Intent intent = new Intent(this, MatrixService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(bound) {
			unbindService(connection);
			bound = false;
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		{ // did we get a room? I bet we got a room, let's check
			final Intent i = getIntent();
			final String selected_room_id = i.getStringExtra("room_id");
			if(selected_room_id != null) {
				Log.i("Main", "Want room " + selected_room_id);
				this.room_id = selected_room_id;
			} else {
				Log.i("Main", "No room selected, picking semi-random default");
				this.room_id = "!DaawmLLRPRTZPOtSVk:perlsite.co.uk";
			}
		}
		final View container = findViewById(R.id.container);
		final ActionBar actionBar = getActionBar();
		final Model.Room room = Model.model.roomByID(room_id);
		if(room == null) {
			actionBar.setTitle("Loading...");
		} else {
			actionBar.setTitle(room.name());
		}
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		final RoomListAdapter nda = new RoomListAdapter(this);
		final ListView room_list = (ListView) findViewById(R.id.left_drawer);
		final DrawerHandler<Model.Room> dh = new DrawerHandler<Model.Room>(
			this,
			(DrawerLayout) findViewById(R.id.drawer_layout),
			room_list,
			container,
			nda,
			true
		);
		room_list.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(
				final AdapterView parent,
				final View view,
				final int position,
				final long id
			) {
				final Model.Room room = nda.getItem(position);
				final Fragment f = new RoomFragment(room.room_id);
				room_id = room.room_id;
				actionBar.setTitle(room.name());

				final Bundle args = new Bundle();
				f.setArguments(args);

				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(
						android.R.anim.slide_in_left,
						android.R.anim.slide_out_right
					)
					.replace(R.id.container, f)
					.commit();

				// Highlight the selected item, update the title, and close the drawer
				room_list.setItemChecked(position, true);
				dh.close();
			}
		});

        if (savedInstanceState == null) {
            getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.container, new RoomFragment(room_id))
				.commit();
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
			final Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
            return true;
        } else if(id == R.id.action_logout) {
			final Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
            return true;
        } else if(id == R.id.action_screenshot) {
			final Bitmap bmp = Screenshot.grab(this);
			final ByteArrayOutputStream out = new ByteArrayOutputStream(); 
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			sendImage(out.toByteArray());
            return true;
        } else if (id == R.id.action_exit) {
			/* so graceful */
			Process.killProcess(Process.myPid());
			System.exit(1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	public void sendMessage(final String txt) {
		if(!bound) {
			Log.d("MSG", "Not bound yet");
			return;
		}
		if(service == null) {
			Log.d("MSG", "No service yet");
			return;
		}
		// service.sendText("!DaawmLLRPRTZPOtSVk:perlsite.co.uk", txt);
		service.sendText(room_id, txt);
	}

	public void sendImage(final byte[] data) {
		if(!bound) {
			Log.d("MSG", "Not bound yet");
			return;
		}
		if(service == null) {
			Log.d("MSG", "No service yet");
			return;
		}
		service.sendPNG(room_id, data);
	}
}

