package org.matrix.client.android;

import android.app.NotificationManager;
import android.app.Service;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Binder;
import android.os.IBinder;

import android.support.v4.app.NotificationCompat;

import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;

import org.matrix.client.API.Event;
import org.matrix.client.API.EventResponse;
import org.matrix.client.API.ImageMessage;
import org.matrix.client.API.MatrixClient;
import org.matrix.client.API.MemberEvent;
import org.matrix.client.API.Message;
import org.matrix.client.API.MessageEvent;
import org.matrix.client.API.Presence;
import org.matrix.client.API.RoomNameEvent;
import org.matrix.client.API.RoomState;
import org.matrix.client.API.SyncResponse;
import org.matrix.client.API.TextMessage;

import retrofit.RetrofitError;

import rx.subjects.Subject;
import rx.schedulers.Schedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;

public class MatrixService extends Service {
	private static final String TAG = "MatrixService";

    private int startMode;
    private final IBinder binder = new MatrixBinding();
    private boolean allowRebind;
	private final Model model = Model.model;

	private Thread networkThread;

    public class MatrixBinding extends Binder {
        public MatrixService getService() {
			Log.d(TAG, "Requested binding");
            return MatrixService.this;
        }
    }

	public MatrixService() {
		super();
		startMode = Service.START_STICKY;
		allowRebind = true;
		this.networkThread = null;
	}

    @Override
    public void onCreate() {
		Log.d(TAG, "Creating service");
		run();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
		Log.d(TAG, "Start command");
		return super.onStartCommand(intent, flags, startId);
    }

	@Override
    public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
        return allowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
		Log.d(TAG, "onDestroy");
    }

	private MatrixClient matrix = null;
	public MatrixClient matrix() { return matrix; }

	public Runnable updateWorker = new Runnable() {
		@Override
		public void run() {
			worker();
			MatrixService.this.networkThread = null;
		}

		private void worker() {
			Log.d(TAG, "Thread starting");

			/**
			 * Just log in with this account if it exists already
			 */
			try {
				matrix.login("@android:perlsite.co.uk", "kapeus9I");
			} catch(final RetrofitError e) {
				Log.d(TAG, "Can't log in, trying to register instead: " + e);
				/*
				 * So that account doesn't work, maybe we can register a new one?
				 */
				try {
					matrix.register("android", "kapeus9I");
					matrix.displayName("Tom M (Android)");
					matrix.joinRoom("#client_test:perlsite.co.uk");
					matrix.joinRoom("#matrix-dev:matrix.org");
					matrix.joinRoom("#matrix:matrix.org");
				} catch(final RetrofitError re) {
					Log.d(TAG, "Had an exception while registering, giving up: " + re);
					return;
				}
			}

			/**
			 * Our initial sync really only needs to happen when we're logging in for
			 * the first time
			 */
			try {
				/* Get ready to send things */
				sender();

				final SyncResponse syncresp = matrix.sync();
				for(final Presence p : syncresp.presence) {
					Log.i(TAG, "Presence: " + p.content.user_id);
					model.updatePresence(
						p.content.user_id,
						p.content.avatar_url,
						p.content.displayname,
						p.content.presence,
						p.content.last_active_ago
					);
				}

				List<RoomState> states = syncresp.rooms;
				for(final RoomState room_state : states) {
					Log.d(TAG, "Room " + room_state.room_id + " membership " + room_state.membership + ":");

					Model.Room room = model.roomByID(room_state.room_id);
					if(room == null) {
						room = new Model.Room(room_state.room_id, "?");
						model.addRoom(room);
					}
					for(final Event ev : room_state.state) {
						handleRoomEvent(ev);
					}
					for(final Event ev : room_state.messages.chunk) {
						handleRoomEvent(ev);
					}
				}
			} catch(final RetrofitError e) {
				Log.e(TAG, "Unable to get initial sync: " + e);
				return;
			}

			Log.d(TAG, "Entering poll loop");
			while(true) {
				try {
					Log.d(TAG, "Waiting...");
					final EventResponse resp = matrix.poll();
					Log.d(TAG, "... done");
					for(final Event ev : resp.chunk) {
						handleRoomEvent(ev);
					}
				} catch(final RetrofitError e) {
					Log.e(TAG, "Poll failed: " + e);
					return;
				}
			}
		}
	};

	public void run() {
		if(this.networkThread == null) {
			Log.d(TAG, "Starting new background thread");
		// this.matrix = new MatrixClient("http://192.168.1.7:8008");
			this.matrix = new MatrixClient("https://matrix.perlsite.co.uk");
			this.networkThread = new Thread(updateWorker);
			Log.d(TAG, "Starting thread");
			this.networkThread.start();
		} else {
			Log.d(TAG, "Background thread exists already, doing nothing");
		}
	}

	private final PublishSubject<Runnable> pendingSend = PublishSubject.create();

	private void sender() {
		pendingSend
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.io())
			.subscribe(new Action1<Runnable>() {
				@Override
				public void call(final Runnable r) {
					Log.d("Sender", "Had outgoing message");
					r.run();
				}
			});
	}

	public void sendText(final String room, final String txt) {
		pendingSend.onNext(new Runnable() {
			@Override
			public void run() {
				Log.d("SEND", "Response: " + matrix().sendText(room, txt));
			}
		});
	}

	public void sendPNG(final String room, final byte[] data) {
		pendingSend.publish(new Runnable() {
			@Override
			public void run() {
				Log.d("SEND", "Response: " + matrix().sendPNG(room, data));
			}
		});
	}

	/* oh java */
	private class Dispatcher {
		public Dispatcher() { }

		public void dispatch(
			final String method,
			final Event o,
			final Model.Room r,
			final Model.User u
		) {
			try {
				final Method m = MatrixService.class.getMethod(
					method,
					new Class[] {
						Model.Room.class,
						Model.User.class,
						o.getClass()
					}
				);
				m.invoke(MatrixService.this, new Object[] { r, u, o });
			} catch(final NoSuchMethodException e) {
				Log.e("ED", "No method for " + o + " - " + e);
			} catch(final IllegalAccessException e) {
				Log.e("ED", "Dodgy access for " + method + " - " + e);
			} catch(final InvocationTargetException e) {
				Log.e("ED", "Dodgy access for " + method + " - " + e);
			}
		}
	}

	private final Dispatcher dispatcher = new Dispatcher();

	private void handleRoomEvent(final Event ev) {
		if(ev == null) {
			Log.e("Matrix", "Null event?");
			return;
		}
		if(ev.room_id == null) {
			Log.e("Matrix", "This event has no room_id?");
			return;
		}
		final Model.Room room = model.roomByID(ev.room_id);
		if(room == null) {
			Log.e("Matrix", "Room not found for event");
			return;
		}
		Model.User user = room.userByID(ev.user_id);
		/* Autocreate users? */
		if(user == null) {
			model.updatePresence(
				ev.user_id,
				null,
				null,
				null,
				null
			);
			user = model.userByID(ev.user_id);
		}
		/* Check again, maybe we failed - although that should be handled by exceptions */
		if(user == null) {
			Log.e("Matrix", "User not found for event");
			return;
		}
		dispatcher.dispatch("dispatchRoomEvent", ev, room, user);
	}

	public void dispatchRoomEvent(
		final Model.Room room,
		final Model.User user,
		final MessageEvent ev
	) {
		final Message msg = ev.content;
		if(msg == null) {
			Log.e(TAG, "Null message - ID was " + ev.event_id);
		} else if(msg.isImage()) {
			final ImageMessage m = (ImageMessage) msg;
			Log.d(TAG, ev.type + ": " + ev.user_id + " - image: " + m.url);
		} else if(msg.isText()) {
			final String m = ((TextMessage) msg).body;
			Log.d(TAG, ev.type + ": " + ev.user_id + " - " + m);
			final Model.Room.Message rm = new Model.Room.Message(
				user,
				ev.event_id,
				msg.msgtype,
				ev.user_id,
				msg.hsob_ts,
				m
			);
			room.onMessage(rm);

			/**
			 * If we have a message for a room that's not currently
			 * displayed, this might be of interest to the user.
			 *
			 * We start building a notification, waiting for the profile
			 * image if available, 
			 */
			final NotificationCompat.Builder notification =
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(room.name())
				.setContentText(m)
				.setNumber(2)
				.setAutoCancel(true);
			final NotificationManager noman = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			final Intent resultIntent = new Intent(this, MainActivity.class);
			final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			final PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
					0,
					PendingIntent.FLAG_UPDATE_CURRENT
				);
			notification.setContentIntent(resultPendingIntent);

			/* Some day we might have more than one notification */
			final int id = 13287;
			if(user.avatar == null) {
				notification.setLargeIcon(
					drawableToBitmap(
						getResources().getDrawable(
							R.drawable.default_profile
						)
					)
				);
				noman.notify(id, notification.build());
			} else {
				Picasso
					.with(this)
					.load(
						user.avatar
					)
					.placeholder(R.drawable.default_profile)
					.error(R.drawable.default_profile)
					.transform(new CropSquareTransformation())
					.into(new Target() {
						@Override
						public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
							notification.setLargeIcon(bitmap);
							noman.notify(id, notification.build());
						}

						@Override
						public void onBitmapFailed(final Drawable d) {
							notification.setLargeIcon(drawableToBitmap(d));
							noman.notify(id, notification.build());
						}

						@Override
						public void onPrepareLoad(final Drawable d) { }
					});
			}
		} else {
			Log.d(TAG, ev.type + ": " + ev.user_id + " - unknown?");
		}
	}

	public void dispatchRoomEvent(
		final Model.Room room,
		final Model.User user,
		final MemberEvent ev
	) {
		user.updateAvatar(ev.content.avatar_url);
		user.updateDisplayname(ev.content.displayname);
		if(ev.membership.equals("join")) {
			room.join(user);
		}
	}

	public void dispatchRoomEvent(
		final Model.Room room,
		final Model.User user,
		final RoomNameEvent ev
	) {
		room.setName(ev.name());
	}

	public Bitmap drawableToBitmap(final Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		final Bitmap bitmap = Bitmap.createBitmap(
			drawable.getIntrinsicWidth(),
			drawable.getIntrinsicHeight(),
			Bitmap.Config.ARGB_8888
		);
		final Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
}

