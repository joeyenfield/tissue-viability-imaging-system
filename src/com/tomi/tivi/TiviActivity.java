package com.tomi.tivi;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class TiviActivity extends Activity {
	int tiviDivides = 1000;
	CameraViewer cameraView;
	TiViProcessor tiviView;
	ToggleButton cameraButton;
	SeekBar cameraZoom;
	SeekBar exposure;
	Spinner videoMode;
	
	ToggleButton tiviButton;
	SeekBar tiviMin;
	SeekBar tiviMax;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tivi);

		cameraView = (CameraViewer) findViewById(R.id.cameraViewer);
		tiviView = (TiViProcessor) findViewById(R.id.tiviViewer);
		cameraButton = (ToggleButton) findViewById(R.id.processingButton);
		cameraZoom = (SeekBar) findViewById(R.id.zoomValueBar);
		exposure = (SeekBar) findViewById(R.id.expValueBar);
		videoMode = (Spinner) findViewById(R.id.videoModes);
		
		tiviButton = (ToggleButton) findViewById(R.id.tiviProcessing);
		tiviMin = (SeekBar) findViewById(R.id.tiviMinValue);
		tiviMax = (SeekBar) findViewById(R.id.tiviMaxValue);

		// 2*tiviDivides is so that you have positive + negavie
		tiviMin.setMax(2*tiviDivides);
		tiviMax.setMax(2*tiviDivides);
		
		tiviMin.setProgress(0);
		tiviMax.setProgress(2*tiviDivides);
		tiviView.setMinVal(-1f);
		tiviView.setMaxVal(1f);
		
		/**
		 * Setup Camera
		 */
		cameraView.initCamera();

		/**
		 * Setup available modes
		 */
		ArrayList<SizeHolder> sizeData = cameraView.getAvailableVideoModes();
		cameraView.setPreviewSize(sizeData.get(0).size);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeData);
		videoMode.setAdapter(adapter);

		/**
		 * Setup TIVI panel
		 */
		cameraView.setTiviProcessor(tiviView);

		
		createListeners();
	}

	public void createListeners() {
		videoMode.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				cameraView.setPreviewSize(((SizeHolder) videoMode.getSelectedItem()).size);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
			}

		});

		cameraButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					cameraView.startCamera();
				} else {
					cameraView.pauseCamera();
				}
			}
		});
		cameraZoom.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				cameraView.setZoom(progress / (float) seekBar.getMax());
			}
		});

		exposure.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				cameraView.setExposureCompensation(progress / (float) seekBar.getMax());
			}
		});
		
		tiviMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tiviView.setMinVal(progress / (float) tiviDivides);
			}
		});
		
		tiviMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tiviView.setMaxVal(progress / (float) tiviDivides);
			}
		});


	}

	private void updateFullscreenStatus(boolean bUseFullscreen) {
		if (bUseFullscreen) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		cameraView.requestLayout();
	}
}
