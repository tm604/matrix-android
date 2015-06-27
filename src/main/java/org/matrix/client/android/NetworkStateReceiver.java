package org.matrix.client.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		final ConnectivityManager conman = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(conman != null) {
			final NetworkInfo ni = conman.getActiveNetworkInfo();
			if(ni != null) {
				Log.d("NSR", "Network: " + ni);
				switch(ni.getType()) {
				case ConnectivityManager.TYPE_WIFI:
					Log.d("NSR", "We have wifi");
					break;
				case ConnectivityManager.TYPE_MOBILE:
					Log.d("NSR", "We are mobile");
					break;
				default:
					Log.d("NSR", "Unknown type: " + ni.getType());
				}
			} else {
				Log.w("NSR", "No active network");
			}
		} else {
			Log.d("NSR", "No conman");
		}
	}
}

