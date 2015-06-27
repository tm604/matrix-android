package org.matrix.client.android;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class HomeActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
	}
}

