package edu.usc.csci576.fast.media.browsing.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
	public List<String> faceDetect(String dir) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("XML Path = " + FaceDetector.class
						.getResource("haarcascade_frontalface_alt.xml")
						.getPath().substring(1));
		CascadeClassifier faceDetector = new CascadeClassifier(FaceDetector.class
						.getResource("haarcascade_frontalface_alt.xml")
						.getPath().substring(1));

		File imgDir = new File(dir);
		List<String> faceImages = new ArrayList<String>();

		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			MatOfRect faceDetections = new MatOfRect();
			faceDetector.detectMultiScale(image, faceDetections);

			if (faceDetections.toArray().length > 0)
				faceImages.add(fileEntry.getAbsolutePath());

		}
		return faceImages;

	}
}