package org.matrix.client.android;

import android.app.Activity;

import android.content.res.TypedArray;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Point;

import android.view.Display;
import android.view.View;

public class Screenshot {
	public static Bitmap grab(final Activity ctx) {
		/* Work out what size we're going to be dealing with */
		final Display display = ctx.getWindowManager().getDefaultDisplay();
		final Point size = new Point();
		display.getSize(size);

		/* Top-level user element to render */
		final View view = ctx.findViewById(android.R.id.content).getRootView();

		// Create the bitmap to use to draw the screenshot
		final Bitmap bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_4444);
		final Canvas canvas = new Canvas(bitmap);

		/* Background depends on theme */
		final Drawable background = ctx.getResources().getDrawable(
			ctx.getTheme().obtainStyledAttributes(new int[] {
				android.R.attr.windowBackground
			})
			.getResourceId(0, 0)
		);

		/* So we render the background first... */
		background.draw(canvas);

		/* ... then the view on top... */
		view.draw(canvas);

		/* ... then we end up with a rendered bitmap */
		return bitmap;
	}
}
