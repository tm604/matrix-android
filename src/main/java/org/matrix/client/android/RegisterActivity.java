package org.matrix.client.android;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class RegisterActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/* No actionbar for this one */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, 
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		);

        setContentView(R.layout.activity_register);
	}
}

