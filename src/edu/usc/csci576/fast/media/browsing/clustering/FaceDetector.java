package edu.usc.csci576.fast.media.browsing.clustering;

import java.nio.file.Path;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
	public boolean hasFace(Path jpegImagePath) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/*System.out.println("XML Path = " + FaceDetector.class
						.getResource("haarcascade_frontalface_alt.xml")
						.getPath().substring(1));*/
		CascadeClassifier faceDetector = new CascadeClassifier(FaceDetector.class
						.getResource("haarcascade_frontalface_alt.xml")
						.getPath().substring(1));

		Mat image = Highgui.imread(jpegImagePath.toString());
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		if (faceDetections.toArray().length > 0) {
			return true;
		}
		return false;
	}
}