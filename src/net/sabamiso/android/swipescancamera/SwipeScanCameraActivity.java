package net.sabamiso.android.swipescancamera;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;

@SuppressWarnings("deprecation")
public class SwipeScanCameraActivity extends Activity implements
		CameraPreviewListener {

	SlitScanView slit_scan_view;
	CameraPreviewView preview_view;
	View gui_view;
	
	Button buttonClear;
	Button buttonSave;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		int screen_w = disp.getWidth();
		int screen_h = disp.getHeight();

		super.onCreate(savedInstanceState);

		OpenCVLoader.initDebug();

		// create view
		AbsoluteLayout layout = new AbsoluteLayout(this);
		layout.setBackgroundColor(Color.BLACK);
		setContentView(layout);

		slit_scan_view = new SlitScanView(this, 640, 480);
		layout.addView(slit_scan_view, new AbsoluteLayout.LayoutParams(
				screen_w, screen_h, 0, 0));

		preview_view = new CameraPreviewView(this, 640, 480, this);
		layout.addView(preview_view, new AbsoluteLayout.LayoutParams(
				screen_w / 4, screen_h / 4, 0, 0));

		gui_view = this.getLayoutInflater().inflate(R.layout.activity_swipe_scan_camera, null);
		layout.addView(gui_view, new AbsoluteLayout.LayoutParams(
				screen_w, screen_h, 0, 0));

		// set gui event listener...
		buttonClear = (Button)gui_view.findViewById(R.id.buttonClear);
		buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slit_scan_view.clear();
			}
		});
		
		buttonSave = (Button)gui_view.findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slit_scan_view.save();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideSystemUI();
	}

	@Override
	protected void onPause() {
		preview_view.stop();
		super.onPause();
		finish();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		hideSystemUI();
	}

	private void hideSystemUI() {
		// see
		// also...https://developer.android.com/training/system-ui/immersive.html
		View decor = this.getWindow().getDecorView();
		decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	@Override
	public void onPreviewFrame(Mat image) {
		if (image == null || image.empty() == true)
			return;
		slit_scan_view.setImage(image);
	}
}
