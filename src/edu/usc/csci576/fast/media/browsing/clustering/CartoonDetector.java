package edu.usc.csci576.fast.media.browsing.clustering;

import java.nio.file.Path;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class CartoonDetector {
	
	public boolean hasCartoons(Path jpegImagePath) {
		Mat src = Highgui.imread(jpegImagePath.toString());
		Mat dst = new Mat();
		Imgproc.bilateralFilter(src, dst, 5, 150, 150);

		double meanValue = 0;
		
		Mat srcHSV=new Mat();
		Mat dstHSV=new Mat();
		 
		Imgproc.cvtColor(src, srcHSV, Imgproc.COLOR_RGB2YUV);
		Imgproc.cvtColor(dst, dstHSV, Imgproc.COLOR_RGB2YUV);

		for (int i = 0; i < 288; i++) {
			for (int j = 0; j < 352; j++) {
				meanValue += srcHSV.get(i, j)[0] - dstHSV.get(i, j)[0];
			}
		}
		
		if(meanValue < Constants.CARTOON_THRESHOLD){
			return true;
		}
		return false;
		
	}

}
