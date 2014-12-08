package edu.usc.csci576.fast.media.browsing.clustering;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.usc.csci576.fast.media.browsing.ui.MainUI;

public class BuildingsDetector {

	private Path baseFolderPath;
	private List<Mat> baseHistList;
	
	public BuildingsDetector() {
		this.baseFolderPath = Paths.get("baseFolder").toAbsolutePath();
		populateBaseHistList();
	}

	private void populateBaseHistList() {
		MainUI.deleteAllJPEGFiles(baseFolderPath);
		File[] baseFileList = baseFolderPath.toFile().listFiles();
		if(baseFileList.length == 0) {
			System.err.println("Have no base referece images to generate histograms");
		}
		baseHistList = new ArrayList<>();
		
		for(File baseImage : baseFileList) {
			Path jpegBaseImage = MainUI.convertRGBToJPEG(baseImage);
			Mat baseHist = generateHist(jpegBaseImage.toFile());
			baseHistList.add(baseHist);
		}
		
	}
	
	/* input image must be jpg format*/
	private Mat generateHist(File inputImage) {
		
		MatOfInt histSize = new MatOfInt(50, 60);
		MatOfFloat ranges = new MatOfFloat(0f, 180f, 0f, 256f);
		MatOfInt channels = new MatOfInt(0, 1);
		
		Mat jpegMat = Highgui.imread(inputImage.toString());
		Mat jpegHSVMat = new Mat();
		Imgproc.cvtColor(jpegMat, jpegHSVMat, Imgproc.COLOR_BGR2HSV);
		
		List<Mat> baseImageList = new ArrayList<>();
		baseImageList.add(jpegHSVMat);

		Mat hist = new Mat();
		Imgproc.calcHist(baseImageList, channels, new Mat(), hist, histSize, ranges);
		//Core.normalize(hist, hist, 0, 1, 32,-1);
		return hist;
		
	}

	public boolean hasBuildings(Path jpegImagePath) {
		Mat inputHist = generateHist(jpegImagePath.toFile());
		boolean hasBuildings = false;
		int j = 0;
		
		for(Mat baseHist: baseHistList) {
			//System.out.println("\n\nComparing input file " + jpegImagePath.getFileName().toString() + " with base hist " + (j+1));
			
			/*double similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_BHATTACHARYYA);
			System.out.println("Similarity by bhattacharya is " + similarity);
			if(similarity >= Constants.BATTACHARYA_THRESHOLD) {
				hasBuildings = true;
				break;
			}
			
			similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_CHISQR);
			System.out.println("Similarity by chisqr is " + similarity);
			if(similarity >= Constants.CHISQR_THRESHOLD) {
				hasBuildings = true;
				break;
			}
			
			similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_CORREL);
			System.out.println("Similarity by correlation is " + similarity);
			if(similarity >= Constants.CORREL_THRESHOLD) {
				hasBuildings = true;
				break;
			}

			similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_HELLINGER);
			System.out.println("Similarity by hellinger is " + similarity);
			if(similarity >= Constants.HELLINGER_THRESHOLD) {
				hasBuildings = true;
				break;
			}*/
			
			
			double similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_INTERSECT);
			//System.out.println("Similarity by intersection is " + similarity);
			if(similarity >= Constants.INTERSECTION_THRESHOLD) {
				hasBuildings = true;
				break;
			}
			
			j++;
		}
		return hasBuildings;
	}
}
