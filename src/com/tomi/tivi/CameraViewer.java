package com.tomi.tivi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// ----------------------------------------------------------------------

public class CameraViewer extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;

	Camera mCamera;

	int frameWide = 0;
	int frameHigh = 0;

	boolean flashFail = false;

	public CameraViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(true);
		// Install a SurfaceHolder.Callback so we
		// get notified when the
		// underlying surface is created and
		// destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void initCamera(){
		mCamera = Camera.open();
	}
	public void setTiviProcessor(TiViProcessor processor) {
		mCamera.setPreviewCallback(processor);
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		int minData = 0;
		Size minSize = null;
		for (Size size : sizes) {
			Log.e(getClass().getSimpleName(), "Available size : " + size.width + "," + size.height);
			int data = size.width * size.height;
			if (data < minData || minSize == null) {
				minData = data;
				minSize = size;
			}
		}

		Log.e(getClass().getSimpleName(), "Chosen size : " + minSize.width + "," + minSize.height);
		return minSize;
	}

	public void setPreviewSize(Size optimalSize) {		
		frameWide = optimalSize.width;
		frameHigh = optimalSize.height;
		
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		mCamera.setParameters(parameters);
	}

	public ArrayList<SizeHolder> getAvailableVideoModes() {
		ArrayList<SizeHolder> modes = new ArrayList<SizeHolder>();
		Parameters p = mCamera.getParameters();
		
		ArrayList<Size> availableSize = new ArrayList<Size>(p.getSupportedPreviewSizes());
		Collections.sort(availableSize, new Comparator<Camera.Size>() {
			public int compare(Size lhs, Size rhs) {
				return lhs.width*lhs.height-rhs.width*rhs.height;
			}
		});
		
		for (Size size : availableSize) {
			modes.add(new SizeHolder(size));
		}
		return modes;
	}

	

	public void surfaceCreated(SurfaceHolder holder) {

		Parameters p = mCamera.getParameters();

		frameWide = getWidth();
		frameHigh = getHeight();

		Size size = getOptimalPreviewSize(p.getSupportedPreviewSizes(), frameWide, frameHigh);
		p.setPreviewSize(size.width, size.height);
		try {
			p.setWhiteBalance(p.WHITE_BALANCE_CLOUDY_DAYLIGHT);
			p.setAntibanding(p.ANTIBANDING_OFF);
		} catch (Exception e) {
			Log.e("Camera_Init", "Setting parameters failed");
		}
		mCamera.setParameters(p);

		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startCamera() {
		mCamera.startPreview();
		try {
			Parameters p = mCamera.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(p);
			flashFail = false;

		} catch (Exception e) {
			flashFail = true;
		}
	}

	public void pauseCamera() {
		mCamera.stopPreview();
		try {
			Parameters p = mCamera.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_OFF);

			mCamera.setParameters(p);
			flashFail = false;
		} catch (Exception e) {
			flashFail = true;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	public void setZoom(float val) {
		if (val < 0) {
			val = 0;
		}
		if (val > 1) {
			val = 1;
		}
		Parameters p = mCamera.getParameters();
		p.setZoom((int) (val * p.getMaxZoom()));
		mCamera.setParameters(p);
	}

	public void setExposureCompensation(float val) {
		if (val < 0) {
			val = 0;
		}
		if (val > 1) {
			val = 1;
		}
		Parameters p = mCamera.getParameters();
		p.setExposureCompensation((int) (p.getMinExposureCompensation() + val * (p.getMaxExposureCompensation() - p.getMinExposureCompensation())));

		mCamera.setParameters(p);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}



}