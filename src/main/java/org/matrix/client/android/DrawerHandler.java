package org.matrix.client.android;

import android.app.Activity;

import android.os.Build;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.view.animation.TranslateAnimation;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DrawerHandler<T> {
	private DrawerLayout layout;
	private ActionBarDrawerToggle toggle;
	private float lastTranslate = 0.0f;
	private ListView list;
	private View child;
	private ArrayAdapter<T> adapter;
	private boolean left = true;
	private final Activity ctx;

	public DrawerHandler(
		final Activity ctx,
		final DrawerLayout layout,
		final ListView list,
		final View child,
		final ArrayAdapter<T> adapter,
		boolean left
	) {
		this.ctx = ctx;
		this.layout = layout;
		this.list = list;
		this.child = child;
		this.adapter = adapter;
		this.left = left;

		layout.setDrawerShadow(
			left ? R.drawable.drawer_shadow : R.drawable.drawer_shadow_right,
			left ? GravityCompat.START : GravityCompat.END
		);
		toggle = new ActionBarDrawerToggle(
			ctx,
			layout,
			R.drawable.ic_navigation_drawer,
			R.string.drawer_close,
			R.string.drawer_open
		) {
			/** Called when a drawer has settled in a completely closed state. */
			@Override
			public void onDrawerClosed(final View view) {
				// super.onDrawerClosed(view);
				// getActionBar().setTitle(mTitle);
				ctx.invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(final View drawerView) {
				super.onDrawerOpened(drawerView);
				// getActionBar().setTitle(mDrawerTitle);
				ctx.invalidateOptionsMenu();
			}

			// @SuppressLint("NewApi")
			public void onDrawerSlide(final View drawer, float slideOffset)
			{
				float moveFactor = (drawer.getWidth() * (DrawerHandler.this.left ? slideOffset : -slideOffset));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					child.setTranslationX(moveFactor);
				} else {
					final TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
					anim.setDuration(0);
					anim.setFillAfter(true);
					child.startAnimation(anim);

					lastTranslate = moveFactor;
				}
			}
		};
		layout.setDrawerListener(toggle);
		list.setAdapter(adapter);
	}

	public void close() {
		layout.closeDrawer(list);
	}
}
