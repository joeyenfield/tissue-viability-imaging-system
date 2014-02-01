package com.tomi.tivi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TiViProcessor extends View implements Camera.PreviewCallback, Runnable {
	// Thread Parameters
	Object allowProcessingLock = new Object();
	Thread redraw;

	int wide = 0;
	int high = 0;

	byte[] activeFrame = null;
	int[] rgb = null;

	float minVal = -1;
	float maxVal = 1;

	int[] histogram;

	boolean processing = false;

	Rect screen;
	Bitmap currentFrame;

	ColorMap map = ColorMap.getColorMap(ColorMap.TYPE_TIVI);

	public TiViProcessor(Context context, AttributeSet attrs) {
		super(context, attrs);
		redraw = new Thread(this);
		redraw.setPriority(Thread.NORM_PRIORITY - 1);
		redraw.start();

		screen = new Rect();
	}

	@Override
	public void onPreviewFrame(byte[] frm, Camera camera) {
		setData(frm, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
		postInvalidate();
	}

	public byte[] getData() {
		return activeFrame;
	}

	public void setData(byte[] frm, int width, int height) {
		if (currentFrame == null || currentFrame.getWidth() != width || currentFrame.getHeight() != height) {
			currentFrame = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			wide = width;
			high = height;
		}

		if (activeFrame == null || activeFrame.length != frm.length) {
			activeFrame = new byte[frm.length];
			rgb = new int[frm.length];

		}

		if (!isProcessing()) {
			setProcessing(true);
			System.arraycopy(frm, 0, activeFrame, 0, activeFrame.length);
			doProcessing();
		}

	}

	public synchronized void setProcessing(boolean processing) {
		this.processing = processing;
	}

	public synchronized boolean isProcessing() {
		return processing;
	}

	public void processFrame() {
		decodeYUV420SP(rgb, activeFrame, wide, high);
		currentFrame.setPixels(rgb, 0, wide, 0, 0, wide, high);
		setProcessing(false);
	}

	public void doProcessing() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				processFrame();
			}
		});
		t.start();
	}

	// Byte decoder :
	//
	// ---------------------------------------------------------------------
	void decodeYUV420SP(int[] rgb, byte[] frame, int width, int height) {
		// Pulled directly from:
		//
		// http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
		final int frameSize = width * height;

		float val = 0;

		int j, yp, uvp, i, y, y1192, r, g, b, u, v;
		for (j = 0, yp = 0; j < height; j++) {
			uvp = frameSize + (j >> 1) * width;
			u = 0;
			v = 0;
			for (i = 0; i < width; i++, yp++) {
				y = (0xff & ((int) frame[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & frame[uvp++]) - 128;
					u = (0xff & frame[uvp++]) - 128;
				}

				y1192 = 1192 * y;
				r = (y1192 + 1634 * v);
				g = (y1192 - 833 * v - 400 * u);
				b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				// Convert to RGB
				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

				/*
				 * Tivi Processing
				 */
				r = Color.red(rgb[yp]);
				g = Color.green(rgb[yp]);
				if (g == 0) {
					g = 1;
				}
				val = (((r - g) / ((float) r + g)) - minVal) / (maxVal - minVal);
				if (val < 0) {
					val = 0;
				}
				if (val > 1) {
					val = 0;
				}
				rgb[yp] = map.getColor(val);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (currentFrame != null) {
			screen.top = 5;
			screen.left = 5;
			screen.bottom = getWidth() - 10;
			screen.right = getHeight() - 10;
			canvas.drawBitmap(currentFrame, null, screen, null);
		}
	}

	@Override
	public void run() {
		while (true) {
			postInvalidate();
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public float getMinVal() {
		return minVal;
	}

	public void setMinVal(float minVal) {
		this.minVal = minVal;
	}

	public float getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(float maxVal) {
		this.maxVal = maxVal;
	}

}
