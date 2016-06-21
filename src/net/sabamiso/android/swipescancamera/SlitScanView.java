package net.sabamiso.android.swipescancamera;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class SlitScanView extends View {
	Mat capture_img;
	Mat canvas;
	Bitmap bitmap;
	Paint paint;
	Handler handler = new Handler();

	public SlitScanView(Context context, int canvas_w, int canvas_h) {
		super(context);
		canvas = new Mat(canvas_h, canvas_w, CvType.CV_8UC3);
		bitmap = Bitmap.createBitmap(canvas_w, canvas_h,
				Bitmap.Config.ARGB_8888);

		paint = new Paint();
		paint.setFilterBitmap(true);

		clear();
	}

	public synchronized void clear() {
		canvas.setTo(new Scalar(0, 0, 0));
		updateImage();
		Toast.makeText(getContext(), "clear...",  Toast.LENGTH_SHORT).show();
	}

	public synchronized void save() {
		// create SwipeScanCamera directory
		File pictures_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File save_dir = new File(pictures_path.getAbsolutePath() + "/SwipeScanCamera");
		save_dir.mkdir();
		
		//
		SimpleDateFormat sdf = new SimpleDateFormat("'SSC_'yyyyMMdd'_'HHmmss'.jpg'");
		String filename = sdf.format(new Date());
		String save_path = save_dir.toString() + File.separator + filename;
		
		// save
		Mat bgr_img = new Mat();
		Imgproc.cvtColor(canvas, bgr_img, Imgproc.COLOR_RGB2BGR);
		Imgcodecs.imwrite(save_path, bgr_img);
		
		// update mediascanner
		scanFile(save_path);
		
		// show message
		Toast.makeText(getContext(), "save to " + save_path + " ...",  Toast.LENGTH_LONG).show();
	}

	private void scanFile(String path) {
		try {
			File f = new File(path);
			MediaScannerConnection.scanFile(getContext(),
					new String[] { f.getAbsolutePath() }, null, null);
		}
		catch(Exception e) {
		}
	}
	
	public synchronized void setImage(Mat image) {
		capture_img = image;
	}

	private void updateImage() {
		// org.opencv.core.Mat -> android.graphics.Bitmap
		Utils.matToBitmap(canvas, bitmap);

		// update screen image (call onDraw())
		handler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		Rect rect = new Rect(0, 0, getWidth(), getHeight());
		canvas.drawBitmap(bitmap, null, rect, paint);
	}

	boolean is_pressed = false;
	float old_x;
	org.opencv.core.Point old_p = new org.opencv.core.Point();

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		int action = evt.getAction() & MotionEvent.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			is_pressed = true;
			old_p.x = evt.getX();
			old_p.y = evt.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float diff_x = (float) (evt.getX() - old_p.x);
			if (is_pressed && diff_x != 0) {
				processSlitScan(evt.getX(), old_p.x);
				old_p.x = evt.getX();
				old_p.y = evt.getY();
			}
			break;
		case MotionEvent.ACTION_UP:
			is_pressed = false;
			break;
		}
		return true;
	}

	private synchronized void processSlitScan(float screen_x0, double screen_x1) {

		// screen -> image
		int image_x0 = (int) (screen_x0 / getWidth() * canvas.cols());
		int image_x1 = (int) (screen_x1 / getWidth() * canvas.cols());

		// range check
		if (image_x0 == image_x1)
			return;

		if (image_x0 > image_x1) {
			int tmp = image_x1;
			image_x1 = image_x0;
			image_x0 = tmp;
		}
		if (image_x0 < 0) image_x0 = 0;
		if (image_x1 < 1) image_x1 = 1;
		if (canvas.cols() - 2 < image_x0) image_x0 = canvas.cols() - 2;
		if (canvas.cols() - 1 < image_x1) image_x1 = canvas.cols() - 1;
		
		// create slit ROI
		org.opencv.core.Rect roi = new org.opencv.core.Rect();
		roi.x = image_x0;
		roi.y = 0;
		roi.width = image_x1 - image_x0;
		roi.height = canvas.rows();

		// copy slit scan image using the ROI.
		new Mat(capture_img, roi).copyTo(new Mat(canvas, roi));

		updateImage();
	}

}
