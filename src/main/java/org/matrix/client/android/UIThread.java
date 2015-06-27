package org.matrix.client.android;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/** Executor that runs tasks on Android's main thread. */
public final class UIThread {
	public final static Executor executor = new Executor() {
		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void execute(Runnable r) {
			handler.post(r);
		}
	};
}

