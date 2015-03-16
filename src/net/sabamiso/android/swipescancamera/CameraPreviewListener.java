package net.sabamiso.android.swipescancamera;

import org.opencv.core.Mat;

public interface CameraPreviewListener {
	void onPreviewFrame(Mat image);
}
