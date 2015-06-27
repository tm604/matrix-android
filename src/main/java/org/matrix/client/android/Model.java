package org.matrix.client.android;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.functions.Action1;
import rx.functions.Func1;

import rx.Observable;

import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

public class Model {
	public static final Model model = new Model();

	private final Set<Room> currentRooms = new HashSet<>();
	private final Map<String, Room> roomsByID = new HashMap<>();
	public final ReplaySubject<Room> rooms = ReplaySubject.create();
	public final ReplaySubject<User> users = ReplaySubject.create();

	private final Map<String, User> usersByID = new HashMap<>();

	public Room roomByID(final String room_id) {
		return roomsByID.get(room_id);
	}

	public Model addRoom(final Room room) {
		if(!currentRooms.contains(room)) {
			currentRooms.add(room);
			roomsByID.put(room.room_id, room);
			rooms.onNext(room);
		}
		return this;
	}

	public User userByID(final String user_id) {
		return usersByID.get(user_id);
	}

	public Model updatePresence(
		final String user_id,
		final String avatar,
		final String displayname,
		final String presence,
		final Long last_seen
	) {
		if(usersByID.containsKey(user_id)) {
			final User u = usersByID.get(user_id);
			u.updateAvatar(avatar);
			u.updateAvatar(displayname);
			u.updatePresence(presence);
			u.updateLastSeen(last_seen);
		} else {
			final User u = new User(
				user_id,
				avatar,
				displayname
			);
			u.updatePresence(presence);
			u.updateLastSeen(last_seen);
			usersByID.put(user_id, u);
			users.onNext(u);
		}
		return this;
	}

	public static class User {
		public final String user_id;
		public String displayname;
		public String avatar;
		public String presence;
		public Long last_seen;

		public final BehaviorSubject<String> currentPresence = BehaviorSubject.create();
		public final BehaviorSubject<String> currentDisplayname = BehaviorSubject.create();
		public final BehaviorSubject<String> currentAvatar = BehaviorSubject.create();
		public final BehaviorSubject<Long> currentLastSeen = BehaviorSubject.create();

		public User(
			final String user_id,
			final String avatar,
			final String displayname
		) {
			this.user_id = user_id;
			this.avatar = avatar;
			this.displayname = displayname;
			this.presence = null;
			this.last_seen = null;

			this.currentDisplayname.onNext(displayname);
			this.currentAvatar.onNext(avatar);
		}

		public User updatePresence(final String presence) {
			if(this.presence == null || !this.presence.equals(presence)) {
				this.presence = presence;
				if(presence != null) {
					this.currentPresence.onNext(presence);
				}
			}
			return this;
		}
		public User updateAvatar(final String avatar) {
			if(this.avatar == null || !this.avatar.equals(avatar)) {
				this.avatar = avatar;
				if(avatar != null) {
					this.currentAvatar.onNext(avatar);
				}
			}
			return this;
		}
		public User updateDisplayname(final String displayname) {
			if(this.displayname == null || !this.displayname.equals(displayname)) {
				this.displayname = displayname;
				this.currentDisplayname.onNext(displayname);
			}
			return this;
		}
		public User updateLastSeen(final Long last_seen) {
			if(this.last_seen == null || !this.last_seen.equals(last_seen)) {
				this.last_seen = last_seen;
				this.currentLastSeen.onNext(last_seen);
			}
			return this;
		}
	}

	public static class Room {
		public final String room_id;
		public String name;
		public final Boolean is_private;

		private final Map<String, User> usersByID = new HashMap<>();
		public ReplaySubject<User> users = ReplaySubject.create();
		public final ReplaySubject<Message> messages = ReplaySubject.create();
		public final BehaviorSubject<Integer> count = BehaviorSubject.create();
		public final BehaviorSubject<String> currentName = BehaviorSubject.create();

		private final Set<User> currentUsers = new HashSet<>();
		private final Boolean removalEnabled = false;

		public Room(final String room_id, final String name) {
			this.room_id = room_id;
			this.name = name;
			if(name == null) {
				currentName.onNext("?");
			} else {
				currentName.onNext(name);
			}
			this.is_private = false;
		}

		public String name() {
			return (name == null) ? room_id : name;
		}

		public Room setName(final String name) {
			this.name = name;
			this.currentName.onNext(name);
			return this;
		}

		public User userByID(final String user_id) {
			return usersByID.get(user_id);
		}

		/**
		 */
		public Room join(final User m) {
			if(!usersByID.containsKey(m.user_id)) {
				currentUsers.add(m);
				usersByID.put(m.user_id, m);
				users.onNext(m);
				count.onNext(currentUsers.size());
			}
			return this;
		}

		public Room leave(final User m) {
			if(currentUsers.contains(m)) {
				usersByID.remove(m.user_id);
				currentUsers.remove(m);
				count.onNext(currentUsers.size());
				/* Replay all the old users apart from this one */
				if(removalEnabled) {
					ReplaySubject<User> old = users;
					users = ReplaySubject.create();
					old.filter(new Func1<User, Boolean>() {
						@Override
						public Boolean call(final User test) {
							return test != m;
						}
					}).subscribe(new Action1<User>() {
						@Override
						public void call(final User user) {
							users.onNext(user);
						}
					});
					old.onCompleted();
				}
			}
			return this;
		}

		public Room onMessage(final Message m) {
			messages.onNext(m);
			return this;
		}

		public static class Message {
			public User user;
			public String event_id;
			public String type;
			public String user_id;
			public Long ts;
			public String body;

			private boolean mine = false;
			private boolean pending = false;

			public Message(
				final User user,
				final String event_id,
				final String type,
				final String user_id,
				final Long ts,
				final String body
			) {
				this.user = user;
				this.event_id = event_id;
				this.type = type;
				this.user_id = user_id;
				this.ts = ts;
				this.body = body;
			}

			public boolean isMine() { return mine; }
			public boolean isPending() { return pending; } 
		}
	}

	public static void main(String... args) {
	/*
		final Model model = new Model();
		Model.Room r = new Model.Room("!abc:localhost", "#something:localhost");
		model.rooms.onNext(r);
		Model.Room.User m = new Model.Room.User("someone");
		r.join(m);
		Model.Room.Message msg = new Model.Room.Message(m, "this is a test");
		model.rooms.subscribe(new Action1<Model.Room>() {
			@Override
			public void call(final Model.Room room) {
				System.out.println("New room - " + room.name);
				room.messages.subscribe(new Action1<Model.Room.Message>() {
					@Override
					public void call(final Model.Room.Message msg) {
						System.out.println("Room " + room.name + " has a new message: " + msg.body);
					}
				});
				room.users.subscribe(new Action1<Model.Room.User>() {
					@Override
					public void call(final Model.Room.User user) {
						System.out.println("Room " + room.name + " has a new user: " + user.user);
					}
				});
			}
		});
		r.onMessage(msg);
		r.leave(m);
		*/
	}
}
