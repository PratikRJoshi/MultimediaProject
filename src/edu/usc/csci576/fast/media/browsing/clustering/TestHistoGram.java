/**
 * 
 */
package edu.usc.csci576.fast.media.browsing.clustering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.usc.csci576.fast.media.browsing.ui.Collage;

/**
 * @author Sathish Srinivasan
 *
 */
public class TestHistoGram {

	public static void clusterByHistogram(Path baseDir, Path inputDir){
		
		deleteAllJPEGFiles(baseDir);
		deleteAllJPEGFiles(inputDir);
		
		File[] baseFileList = baseDir.toFile().listFiles();
		List<Mat> baseHistList = new ArrayList<>();
		
		for(File baseImage : baseFileList) {
			Mat baseHist = generatHist(baseDir, baseImage);
			baseHistList.add(baseHist);
		}
		
		
		File[] inputFileList = inputDir.toFile().listFiles();
		for(File inputImage : inputFileList) {
			//int j=0;
			boolean flag = false;
			double maxSimilarity = 0;
			for(Mat baseHist: baseHistList) {
				Mat inputHist = generatHist(inputDir, inputImage);
				//System.out.println("\n\nCompared input file " + inputImage.getName().toString() + " with base hist " + (j+1));
	
				/*double similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_BHATTACHARYYA);
				System.out.println("Similarity by bhattacharya is " + similarity);
				
				similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_CHISQR);
				System.out.println("Similarity by chisqr is " + similarity);
				
				similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_CORREL);
				System.out.println("Similarity by correlation is " + similarity);
				
				similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_HELLINGER);
				System.out.println("Similarity by hellinger is " + similarity);*/
				
				double similarity = Imgproc.compareHist(baseHist, inputHist, Imgproc.CV_COMP_INTERSECT);
				if(similarity > 50000) {
					flag = true;
					if(maxSimilarity < similarity) {
						maxSimilarity = similarity;
					}
				}
				//System.out.println("Similarity by intersect is " + similarity);
				//j++;
			}
			//System.out.println("Max similarity is " + maxSimilarity);
			if(flag) {
				System.out.println("yes " + inputImage.getName().toString() + " is a building");
			} else {
				System.out.println("no " + inputImage.getName().toString() + " is not a building");
			}
		}
	}
	
	private static void deleteAllJPEGFiles(Path dir) {
		File[] files = dir.toFile().listFiles();
		for(File file: files) {
			if(file.getName().endsWith(".jpg")) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static Mat generatHist(Path baseDir, File inputImage) {
		
		MatOfInt histSize = new MatOfInt(50, 60);
		MatOfFloat ranges = new MatOfFloat(0f, 180f, 0f, 256f);
		MatOfInt channels = new MatOfInt(0, 1);
		
		Path jpegImagePath = Paths.get(baseDir + "\\" + getFileNameWithoutExtension(inputImage.getName()) + ".jpg");
		createJPEGImageIfNotExists(inputImage.toPath(), jpegImagePath);
		Mat jpegMat = Highgui.imread(jpegImagePath.toAbsolutePath().toString());
		Mat jpegHSVMat = new Mat();
		Imgproc.cvtColor(jpegMat, jpegHSVMat, Imgproc.COLOR_BGR2HSV);

		List<Mat> baseImageList = new ArrayList<>();
		baseImageList.add(jpegHSVMat);

		Mat hist = new Mat();
		Imgproc.calcHist(baseImageList, channels, new Mat(), hist, histSize, ranges);
		//Core.normalize(hist, hist, 0, 1, 32,-1);

		return hist;
		
	}

	private static void createJPEGImageIfNotExists(Path rgbImagePath, Path jpegImagePath) {
		try {
			if (!Files.exists(jpegImagePath)) {
				BufferedImage image = Collage.getImageWithFrameNumber(
						rgbImagePath, 0, 352, 288);
				ImageIO.write(image, "jpg", jpegImagePath.toFile());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getFileNameWithoutExtension(String imagePath) {
		String[] tokens = imagePath.split("\\.");
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<tokens.length-1;i++) {
			builder.append(tokens[0]);
		}
		return builder.toString();
	}
	
	/*private static MediaType getFileType(File fileEntry) {
		if(fileEntry.length() == ImageClassifier.IMAGE_LENGTH) {
			return MediaType.Image;
		} else if(fileEntry.length() == ImageClassifier.COLLAGE_LENGTH) {
			return MediaType.Collage;
		} else {
			return MediaType.Video;
		}
	}*/
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		Path baseDir = Paths.get("C:\\Users\\Sathish\\Documents\\EclipseJavaProjects\\FastMediaBrowsing\\dataset\\histogram\\base");
		Path inputDir = Paths.get("C:\\Users\\Sathish\\Documents\\EclipseJavaProjects\\FastMediaBrowsing\\dataset\\histogram\\input");
		clusterByHistogram(baseDir, inputDir);
	}

}
