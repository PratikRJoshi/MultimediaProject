package edu.usc.csci576.fast.media.browsing.clustering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.usc.csci576.fast.media.browsing.ui.Collage;
import edu.usc.csci576.fast.media.browsing.ui.DisplayMedia;
import edu.usc.csci576.fast.media.browsing.ui.MainUI;
import edu.usc.csci576.fast.media.browsing.ui.Media;
import edu.usc.csci576.fast.media.browsing.ui.MediaType;

public class ImageClassifier {
	
	private static final int IMAGE_HEIGHT = 288;
	private static final int IMAGE_WIDTH = 352;
	private static final long IMAGE_LENGTH = IMAGE_WIDTH * IMAGE_HEIGHT * 3;
	private static final long COLLAGE_LENGTH = Collage.COLLAGED_IMAGE_WIDTH * Collage.COLLAGED_IMAGE_HEIGHT * 3;
	
	static double C[] = new double[8];
	static double COS[][] = new double[8][8];
	static List<String> imagesList = new ArrayList<String>(); 
	/*
	 * private static void convertRGBtoJPG(){
	 * 
	 * imageReader image = new imageReader(); String imageArr[] = new String[2];
	 * 
	 * File folder = new File(
	 * "C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1"
	 * ); int i=1; for (File fileEntry : folder.listFiles()) {
	 * System.out.println(i++ +" "+fileEntry.getName()); String fileName =
	 * fileEntry.getName().split("\\.")[0]; imageArr[0]=
	 * "C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1\\"
	 * +fileEntry.getName(); imageArr[1]=(fileName)+".jpg";
	 * System.out.println(imageArr[1]);
	 * 
	 * image.main(imageArr);
	 * 
	 * }
	 * 
	 * }
	 */

	/*private static int getPeakValueForImage(Mat hueMat) {

		byte data[] = new byte[(int) (hueMat.total() * hueMat.channels())];
		byte countMat[] = new byte[360];
		// System.out.println("rows "+hueMat.rows()+"columns "+hueMat.cols()+" totals "+hueMat.total());
		// hueMat.get(0, 0, data);

		for (int i = 0; i < 288; i++) {
			for (int j = 0; j < 352; j++)
				countMat[(int) hueMat.get(i, j)[0]]++;
		}

		int peakValue = countMat[0];
		int peakIndex = 0;
		for (int i = 0; i < countMat.length; i++) {
			if (peakValue < countMat[i]) {
				peakValue = countMat[i];
				peakIndex = i;
			}
		}
		System.out.println("The peak index for this image is " + peakIndex
				+ " value is " + peakValue);
		return peakIndex;
	}

	private static HashMap<String, Integer> getImages(String dir) {

		File imgDir = new File(dir);
		HashMap<String, Integer> mapping = new HashMap<String, Integer>();
		List<PeakObject> mappingList = new ArrayList<PeakObject>();
		int index = 0;
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat hsvimg = new Mat();
			Imgproc.cvtColor(image, hsvimg, Imgproc.COLOR_BGR2HSV);
			int peakValue = getPeakValueForImage(hsvimg);
			mapping.put(fileEntry.getAbsolutePath(), peakValue);

		}
		System.out.println("Size of map " + mapping.size());
		return mapping;

	}

	private static void createBuckets(HashMap<String, Double> peakMap,
			double lowerLimit, double upperLimit) {
		Iterator it = peakMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((double) pairs.getValue() > lowerLimit
					&& (double) pairs.getValue() <= upperLimit) {
				System.out.println("Image path " + pairs.getKey() + " "
						+ "peakvalue " + pairs.getValue());
				File src = new File((String) pairs.getKey());
				File dest = new File("C:\\outputCartoon\\" + src.getName());

				try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void createBuckets(HashMap<String, Integer> peakMap,
			int lowerLimit, int upperLimit) {
		Iterator it = peakMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((int) pairs.getValue() > lowerLimit
					&& (int) pairs.getValue() <= upperLimit) {
				System.out.println("Image path " + pairs.getKey() + " "
						+ "peakvalue " + pairs.getValue());
				File src = new File((String) pairs.getKey());
				File dest = new File("C:\\output\\" + src.getName());

				try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void separateCartoonImages(HashMap<String, Double> filterMap) {
		Iterator it = filterMap.entrySet().iterator();
		int i = 1;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((double) pairs.getValue() < -0.01) {
				System.out.println("Image path " + pairs.getKey() + " "
						+ "peakvalue " + pairs.getValue());
				File src = new File((String) pairs.getKey());
				File dest = new File("C:\\outputCartoon\\" + src.getName());

				try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Cartoon image found: " + (i++));
		}

	}

	private static void copyFileUsingFileChannels(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	private static void setCOSValues() {
		// double cos[][] = cosArray;
		// System.out.println(Math.acos(-1));
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				COS[i][j] = Math.cos(((2 * j + 1) * i * Math.acos(-1)) / 16.0); // i
																				// and
																				// j
																				// should
																				// interchange
																				// their
																				// positions

				if (i == 0 || j == 0)
					C[i] = 1 / Math.sqrt(2);
				else
					C[i] = 1;
			}
		}

	}
*/
	public static List<String> getImagesInAList(String dir){
		File imgDir=new File(dir);
		List<String> imgList = new ArrayList<String>();
		for (File fileEntry : imgDir.listFiles()) {
			imgList.add(fileEntry.getName());
		}
		return imgList;
	}
	
	
	private static List<String> classifyByFilter(String baseDir) {
		//File imgDir = new File(dir);
		HashMap<String, Double> mapping = new HashMap<String, Double>();
		List<String> cartoonList =new ArrayList<String>();
		for(int index = 0 ; index < imagesList.size() ; index++){
			Mat src = Highgui.imread(baseDir+"/"+imagesList.get(index));
			Mat dst = new Mat();
			Imgproc.bilateralFilter(src, dst, 5, 150, 150);

			double meanValue = 0;
			
			Mat srcHSV=new Mat();
			Mat dstHSV=new Mat();
			 
			Imgproc.cvtColor(src, srcHSV, Imgproc.COLOR_RGB2YUV);
			Imgproc.cvtColor(dst, dstHSV, Imgproc.COLOR_RGB2YUV);

			for (int i = 0; i < 288; i++)
				for (int j = 0; j < 352; j++)
					meanValue += srcHSV.get(i, j)[0] - dstHSV.get(i, j)[0];

			//meanValue /= 352 * 288;
			
			if(meanValue < -4000){
				cartoonList.add(imagesList.get(index));
				imagesList.remove(imagesList.get(index));
			}
			//System.out.println("Mean for the image "+fileEntry.getName()+" is "+meanValue);
			//mapping.put(fileEntry.getAbsolutePath(), meanValue);
		}
		return cartoonList;
		// System.out.println("Mean is "+meanValue);

		// Highgui.imwrite("c:\\testcartoon.jpg", dst);
	}
/*
	private static HashMap<String, Double> getDCTValues(String dir) {
		File imgDir = new File(dir);
		HashMap<String, Double> mapping = new HashMap<String, Double>();
		setCOSValues();
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2YUV);
			// System.out.println("here" +yuvimg.get(0, 0)[0]);
			double dctBuffer[][] = PerformDCT
					.applyDCT(288, 352, yuvimg, COS, C);
			// printMat(dctBuffer);
			double freqValue = PerformDCT.readZigZag(288, 352, dctBuffer);
			System.out.println("Frequency value for image :"
					+ fileEntry.getName() + " is " + freqValue);
			mapping.put(fileEntry.getAbsolutePath(), freqValue);
			// System.exit(0);

		}
		System.out.println("Size of map " + mapping.size());
		return mapping;

	}*/

	/** 
	 * Input directory passed should contain the lists of images in jpeg format
	 **/
	private static Map<Integer, List<String>> sortByKMeans(List<String> inputList, String baseDir) {
//		File imgDir = new File(dir);
		int numberOfBaseImages = 10;
		double meanArr[] = new double[numberOfBaseImages];

		Map<Integer, List<String>> collageList = new HashMap<Integer, List<String>>();

		//List<Integer> randomImageNumbers = getListOfRandomNumbers();
		List<Mat> yuvBaseRefList = new ArrayList<Mat>();
		Random randNumber = new Random();
//		File[] rgbImageFiles = imgDir.listFiles();
		for (int i=0;i<numberOfBaseImages;i++) {
			String baseImage = baseDir+"/"+inputList.get(randNumber.nextInt(inputList.size()));
			Mat baseRef = Highgui.imread(baseImage);
			Mat yuvBaseRef = new Mat();
			Imgproc.cvtColor(baseRef, yuvBaseRef, Imgproc.COLOR_RGB2YUV);
			yuvBaseRefList.add(yuvBaseRef);
		}

//		for (File fileEntry : rgbImageFiles) {
		for(int x = 0; x < inputList.size(); x++){
			Mat image = Highgui.imread(baseDir+"/"+inputList.get(x));
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2YUV);

			for (int i = 0; i < numberOfBaseImages; i++) {
				double tempVal = 0;
				for (int j = 0; j < IMAGE_HEIGHT; j++) {
					for (int k = 0; k < IMAGE_WIDTH; k++) {
						double yuvPixelDiff = (yuvBaseRefList.get(i).get(j, k)[0]) - (yuvimg.get(j, k)[0]);
						tempVal += Math.pow(yuvPixelDiff, 2);
					}
				}
				meanArr[i] = Math.sqrt(tempVal);
			}
			int index = findMin(meanArr);
			System.out.println("Inserting into index " + index);
			if (collageList.get(index) == null) {
				List<String> tempList = new ArrayList<String>();
				tempList.add(inputList.get(x));
				collageList.put(index, tempList);
			} else {
				collageList.get(index).add(inputList.get(x));
			}

		}
		return collageList;

	}

	static List<Integer> getListOfRandomNumbers() {
		List<Integer> randomNumbers = new ArrayList<Integer>();
		for(int i=1;i<=300;i++) {
			randomNumbers.add(i);
		}
		Collections.shuffle(randomNumbers);
		return randomNumbers;
	}

	/*private static void sortByKMeans1(String dir) {
		File imgDir = new File(dir);	
		Mat ref1 = Highgui.imread("C:\\dataset1\\outputImage_1.jpg");
		Mat ref2 = Highgui.imread("C:\\dataset1\\outputImage_23.jpg");
		Mat ref3 = Highgui.imread("C:\\dataset1\\outputImage_123.jpg");
		Mat ref4 = Highgui.imread("C:\\dataset1\\outputImage_199.jpg");
		Mat ref5 = Highgui.imread("C:\\dataset1\\outputImage_248.jpg");

		// Mat ref1=Highgui.imread("C:\\kmeans\\0\\outputImage_67.jpg");
		// Mat ref2=Highgui.imread("C:\\kmeans\\0\\outputImage_98.jpg");
		// Mat ref3=Highgui.imread("C:\\kmeans\\0\\outputImage_123.jpg");
		// Mat ref4=Highgui.imread("C:\\kmeans\\0\\outputImage_151.jpg");
		// Mat ref5=Highgui.imread("C:\\kmeans\\0\\outputImage_232.jpg");

		Mat yuvref1 = new Mat();
		Mat yuvref2 = new Mat();
		Mat yuvref3 = new Mat();
		Mat yuvref4 = new Mat();
		Mat yuvref5 = new Mat();

		Imgproc.cvtColor(ref1, yuvref1, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref2, yuvref2, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref3, yuvref3, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref4, yuvref4, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref5, yuvref5, Imgproc.COLOR_RGB2HSV);

		int pk1 = getPeakValueForImage(yuvref1);
		int pk2 = getPeakValueForImage(yuvref2);
		int pk3 = getPeakValueForImage(yuvref3);
		int pk4 = getPeakValueForImage(yuvref4);
		int pk5 = getPeakValueForImage(yuvref5);

		int meanArr[] = new int[5];

		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2HSV);

			int pkimg = getPeakValueForImage(yuvimg);

			meanArr[0] = Math.abs(pk1 - pkimg);
			meanArr[1] = Math.abs(pk2 - pkimg);
			meanArr[2] = Math.abs(pk3 - pkimg);
			meanArr[3] = Math.abs(pk4 - pkimg);
			meanArr[4] = Math.abs(pk5 - pkimg);
			System.out.println(pk1 + " " + pk2 + " " + pk3 + " " + pk4 + " "
					+ pk5 + " " + pkimg + " ");
			int index = findMin(meanArr);

			File file = new File("C:\\kmeans\\" + index);
			File src = new File(fileEntry.getAbsolutePath());
			if (!file.exists()) {
				if (file.mkdir()) {
					File dest = new File("C:\\kmeans\\" + index + "\\"
							+ src.getName());
					try {
						System.out.println("Creating bucketno " + index);
						copyFileUsingFileChannels(src, dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Failed to create directory!");
				}
			} else {
				File dest = new File("C:\\kmeans\\" + index + "\\"
						+ src.getName());
				try {
					System.out.println("Inserting in bucketno " + index);
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}*/

	private static int findMin(double array[]) {
		double min = Double.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
				minIndex = i;
			}
		}
		return minIndex;

	}

	/*private static int findMin(int array[]) {
		int min = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
				minIndex = i;
			}
		}
		return minIndex;

	}*/

	/*private static void printMat(double input[][]) {
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				System.out.print(input[i][j] + " ");
			}
			System.out.println();
		}
	}

	private static void separateFaceImages(List<String> faceImages) {

		for (int i = 0; i < faceImages.size(); i++) {
			File src = new File(faceImages.get(i));
			File dest = new File("C:\\outputFace\\" + src.getName());

			try {
				copyFileUsingFileChannels(src, dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Iteration " + (i + 1) + " finished");
		}
	}*/

	/*private static void createBuckets(HashMap<String, Integer> imageMap,
			int bucketSize) {
		Iterator it = imageMap.entrySet().iterator();
		new File("c:\\buckets").mkdir();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// if((int)pairs.getValue()>hueValue &&
			// (int)pairs.getValue()<=(hueValue+bucketSize)){
			System.out.println("Image path " + pairs.getKey() + " "
					+ "peakvalue " + pairs.getValue());
			File src = new File((String) pairs.getKey());
			int bucketNo = (int) pairs.getValue() / bucketSize;
			// String bucketNo="bucket"+index;
			File file = new File("C:\\buckets\\" + bucketNo);
			if (!file.exists()) {
				if (file.mkdir()) {
					File dest = new File("C:\\buckets\\" + bucketNo + "\\"
							+ src.getName());
					try {
						System.out.println("Creating bucketno " + bucketNo);
						copyFileUsingFileChannels(src, dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Failed to create directory!");
				}
			} else {
				File dest = new File("C:\\buckets\\" + bucketNo + "\\"
						+ src.getName());
				try {
					System.out.println("Inserting in bucketno " + bucketNo);
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}*/

	public static Collage getFaceCollage(List<String> faceList ,String baseDir){
		List<Media> listOfMedia = new ArrayList<Media>();
		Collage collage = null;
		for (String imagePath : faceList) {
			System.out.println(imagePath);
			Path p = Paths.get(baseDir + "/" + getFileNameWithoutExtension(imagePath) + ".rgb");
			Media m = new Media(p, MediaType.Image);
			listOfMedia.add(m);
		}
		try {
			collage = new Collage(listOfMedia, 100, 60);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return collage;
	}
	
	public static Collage getCartoonCollage(List<String> faceList ,String baseDir){
		List<Media> listOfMedia = new ArrayList<Media>();
		Collage collage = null;
		for (String imagePath : faceList) {
			System.out.println(imagePath);
			Path p = Paths.get(baseDir + "/" + getFileNameWithoutExtension(imagePath) + ".rgb");
			System.out.println("p: "+p.toString());
			Media m = new Media(p, MediaType.Image);
			listOfMedia.add(m);
		}
		try {
			collage = new Collage(listOfMedia, 100, 60);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return collage;
	}
	
	public static void main(String[] args) throws IOException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		//System.exit(0);

		/* Make sure the command line argument is absolute path of the folder*/
		Path mediaFolderPath = Paths.get(args[0]);
		Path collageFolderPath = Paths.get(mediaFolderPath + "/collage");
		Path jpegFolderPath = Paths.get(mediaFolderPath + "/jpeg");

		MainUI ui = MainUI.getInstance();
		ui.setMediaFolderName(mediaFolderPath);
		ui.createFolderIfNotExists(collageFolderPath);
		ui.setCollageFolderName(collageFolderPath);
		ui.createFolderIfNotExists(jpegFolderPath);
		createJPEGForEachRGBImage(mediaFolderPath, jpegFolderPath);
		
		
		
		imagesList = getImagesInAList(jpegFolderPath.toString());
		
		FaceDetector fDetect = new FaceDetector();
		List<String> faceImages = fDetect.faceDetect(imagesList , jpegFolderPath.toString());
		
		//clusterByHistogram(args[0]);
		List<String> cartoonImages = classifyByFilter(jpegFolderPath.toString());
		
		List<Media> rootCollage = new ArrayList<Media>();
		Collage faceCollage=getFaceCollage(faceImages , args[0]);
		Collage cartoonCollage=getCartoonCollage(cartoonImages,args[0]);
		
		Media faceMedia = new Media(faceCollage.getCollagedImageFileName(), MediaType.Collage);
		Media cartoonMedia = new Media(cartoonCollage.getCollagedImageFileName(), MediaType.Collage);
		
		rootCollage.add(faceMedia);
		rootCollage.add(cartoonMedia);

		//System.exit(0);
		
		Map<Integer, List<String>> collageMap = sortByKMeans(imagesList, jpegFolderPath.toString());
		Collage kMeansCollage = getKMeansCollage(collageMap, args[0]);
		Media kMeansMedia = new Media(kMeansCollage.getCollagedImageFileName(), MediaType.Collage);
		rootCollage.add(kMeansMedia);
		
		Collage newCollage = new Collage(rootCollage, 300, 300);
		Media display = new Media(newCollage.getCollagedImageFileName(), MediaType.Collage);
		DisplayMedia image = new DisplayMedia(display);
		image.display();
		
//		List<Media> listOfCollage = new ArrayList<Media>();
//		for (Integer index : collageMap.keySet()) {
//			List<Media> listOfMedia = new ArrayList<Media>();
//			List<String> images = collageMap.get(index);
//			for (String imagePath : images) {
//				System.out.println(imagePath);
//				Path p = Paths.get(args[0] + "/" + getFileNameWithoutExtension(imagePath) + ".rgb");
//				Media m = new Media(p, MediaType.Image);
//				listOfMedia.add(m);
//			}
////
//			/*TODO should remove this part and divide this into much smaller list sizes*/
			
//			Path videoPath = Paths.get("C:\\Pratik\\MultimediaProject\\CS576_Project_Videos_1\\CS576_Project_Videos_1\\video01.rgb");
//			Media video = new Media(videoPath, MediaType.Video);
//			listOfMedia.add(video);
//			Collage collage = new Collage(listOfMedia, 100, 60);
//			Media m = new Media(collage.getCollagedImageFileName(), MediaType.Collage);
//			listOfCollage.add(m);
//		}
//		Collage newCollage = new Collage(listOfCollage, 200, 150);
//		Media display = new Media(newCollage.getCollagedImageFileName(), MediaType.Collage);
//		DisplayMedia image = new DisplayMedia(display);
//		image.display();
	}

	public static Collage getKMeansCollage(Map<Integer, List<String>> map, String baseDir){
		int counter = 1;
		List<Media> listOfMedia = new ArrayList<Media>();
		List<Media> kMeansCollageList = new ArrayList<Media>();
		Collage finalCollage = null ;
		for (Integer index : map.keySet()) {
				List<String> images = map.get(index);
				for (String imagePath : images) {
					counter++;
					Path p = Paths.get(baseDir + "/" + getFileNameWithoutExtension(imagePath) + ".rgb");
					Media m = new Media(p, MediaType.Image);
					listOfMedia.add(m);
					if(counter > 60){
						Collage newCollage;
						try {
							newCollage=new Collage(listOfMedia, 50, 50);
							Media col = new Media(newCollage.getCollagedImageFileName(), MediaType.Collage);
							kMeansCollageList.add(col);
							listOfMedia = new ArrayList<Media>();
							counter=0;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}	
				}
				Collage tempCollage= null;
				try{
					tempCollage=new Collage(listOfMedia, 100, 60);
					Media col = new Media(tempCollage.getCollagedImageFileName(), MediaType.Collage);
					kMeansCollageList.add(col);
					listOfMedia = new ArrayList<Media>();
					}
				catch (IOException e){
					e.printStackTrace();
				}
			
		}
		
		//create a final collage consisting of all the collages in the collage list object
		try {
			finalCollage = new Collage(kMeansCollageList, 150, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	return finalCollage;
		
	}
	
	private static void createJPEGForEachRGBImage(Path mediaFolderPath, Path jpegFolderPath) {
		try {
			for (File fileEntry : mediaFolderPath.toFile().listFiles()) {
				if (!Files.isDirectory(fileEntry.toPath()) && getFileType(fileEntry) != MediaType.Video) {
					String outputFileName = jpegFolderPath + "\\" + getFileNameWithoutExtension(fileEntry.getName()) + ".jpg";
					File outputFile = new File(outputFileName);
					if(!Files.exists(outputFile.toPath())) {
						BufferedImage image = Collage.getImageWithFrameNumber(fileEntry.toPath(), 0, IMAGE_WIDTH, IMAGE_HEIGHT);
						ImageIO.write(image, "jpg", outputFile);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static MediaType getFileType(File fileEntry) {
		if(fileEntry.length() == IMAGE_LENGTH) {
			return MediaType.Image;
		} else if(fileEntry.length() == COLLAGE_LENGTH) {
			return MediaType.Collage;
		} else {
			return MediaType.Video;
		}
	}
		
	public static void clusterByHistogram(String dir){
		Mat refImage = Highgui.imread("C:\\Pratik\\MultimediaProject\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1\\jpeg\\image002.jpg");
		Mat refHSVimg=new Mat();
		Imgproc.cvtColor(refImage, refHSVimg, Imgproc.COLOR_BGR2HSV);
		MatOfInt histSize = new MatOfInt(50,60);
		MatOfFloat ranges= new MatOfFloat(0f,180f,0f,256f);
		MatOfInt channels = new MatOfInt(0,1);
		List<Mat> list1 = new ArrayList<Mat>();
		list1.add(refHSVimg);
		
		Mat hist_test1 = new Mat();
		Imgproc.calcHist(list1, channels, new Mat(), hist_test1, histSize,ranges);
		
		
		File imgDir= new File(dir);
		for (File fileEntry : imgDir.listFiles()) {
			Mat img= Highgui.imread(fileEntry.getAbsolutePath());
			Mat HSVimg= new Mat();
			Imgproc.cvtColor(img, HSVimg, Imgproc.COLOR_BGR2HSV);
			List<Mat> list2 = new ArrayList<>();
			list2.add(HSVimg);
			
			Mat hist_test2 = new Mat();
			Imgproc.calcHist(list2, channels, new Mat(), hist_test2, histSize,ranges);
			
			Core.normalize(hist_test1, hist_test1, 0, 1, 32,-1);
			Core.normalize(hist_test2, hist_test2, 0, 1, 32,-1);
			
			double result=Imgproc.compareHist(hist_test1, hist_test2, 1);
			
			System.out.println("The result for image "+fileEntry.getName()+" is "+result);
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
}

		//
		// HashMap<String,Double> filterMap = classifyByFilter(args[1]);
		// separateCartoonImages(filterMap);
		//
		// FaceDetector detect=new FaceDetector();
		// List<String> faceImages=detect.faceDetect(args[0]);
		// separateFaceImages(faceImages);

		// HashMap<String,Double> dctMap = getDCTValues(args[0]);
		// createBuckets(dctMap,1566.0,1650.0);

		// //convertRGBtoJPG();
		//
		// Mat img_1 = Highgui.imread(args[0]);
		// Mat img_2 = Highgui.imread(args[1]);
		//
		// //Highgui.im
		// Mat hsvimg1 = new Mat();
		// Mat hsvimg2 = new Mat();
		// Mat img_out = new Mat();
		//
		// int minHessian = 400;
		//
		//
		// //Imgproc.cvt
		// // FeatureDetector surf =
		// FeatureDetector.create(FeatureDetector.SURF);
		// //
		// // MatOfKeyPoint kp3=new MatOfKeyPoint();
		// //
		// // surf.detect( img_1, kp3 );
		// // Features2d.drawKeypoints(img_1, kp3, img_out);
		// //
		// // Highgui.imwrite("c:\\output2.png", img_out);
		// //
		// //int code=
		// System.out.println("Number of channels 2 "+img_2.channels());
		// System.out.println("Number of channels "+img_1.channels());
		//
		//
		// //Imgproc.cvtColor(img_1, img_1, Imgproc.COLOR_GRAY2BGR);
		// //Imgproc.cvtColor(img_2, img_2, Imgproc.COLOR_GRAY2BGR);
		//
		// //System.out.println("Number of channels "+img_1.channels());
		//
		//
		// Imgproc.cvtColor(img_1, hsvimg1, Imgproc.COLOR_BGR2HSV);
		// Imgproc.cvtColor(img_2, hsvimg2, Imgproc.COLOR_BGR2HSV);
		//
		// getPeakValueForImage(hsvimg1);
		//
		//
		// /// Using 50 bins for hue and 60 for saturation
		// int h_bins = 50; int s_bins = 60;
		// int histSize1[] = { h_bins, s_bins };
		// MatOfInt hbins=new MatOfInt(50);
		// MatOfInt sbins=new MatOfInt(60);
		//
		// MatOfInt histSize = new MatOfInt(50,60);
		// // hue varies from 0 to 179, saturation from 0 to 255
		// //float h_ranges[] = { 0, 180 };
		// MatOfFloat ranges= new MatOfFloat(0f,180f,0f,256f);
		//
		// //float ranges[][] = { h_ranges, s_ranges };
		//
		// // Use the o-th and 1-st channels
		// //int channels1[] = { 0, 1, 2 };
		// MatOfInt channels = new MatOfInt(0,1);
		//
		// List<Mat> list1 = new ArrayList<>();
		// list1.add(hsvimg1);
		//
		// List<Mat> list2 = new ArrayList<>();
		// list2.add(hsvimg2);
		//
		// /// Histograms
		//
		// Mat hist_test1 = new Mat();
		// Mat hist_test2 = new Mat();
		//
		// /// Calculate the histograms for the HSV images
		// Imgproc.calcHist(list1, channels, new Mat(), hist_test1, histSize,
		// ranges);
		// Imgproc.calcHist(list2, channels, new Mat(), hist_test2, histSize,
		// ranges);
		//
		// //System.out.println("Output"+img_1.+" "+hist_test1.dims());
		// //System.out.println("Output 2"+img_2.dims()+" "+hist_test2.dims());
		// if(hist_test1.equals(hist_test2))
		// System.out.println("Are equal");
		//
		// Core.normalize(hist_test1, hist_test1, 0, 1, 32,-1);
		// Core.normalize(hist_test2, hist_test2, 0, 1, 32,-1);
		//
		// //Core.norm
		// double result1=Imgproc.compareHist(hist_test1, hist_test1, 0);
		// double result2=Imgproc.compareHist(hist_test1, hist_test2, 0);
		//
		// System.out.println("The result is "+result1+" "+ result2);
		// //calcHist( hsvimg1, 1, channels, Mat(), hist_base, 2, histSize,
		// ranges, true, false );
		// //normalize( hist_base, hist_base, 0, 1, NORM_MINMAX, -1, Mat() );
		//
		// //calcHist( hsvimg2, 1, channels, Mat(), hist_test2, 2, histSize,
		// ranges, true, false );
		// //normalize( hist_test2, hist_test2, 0, 1, NORM_MINMAX, -1, Mat() );
		//
		// //outputImage.setIcon(new javax.swing.ImageIcon("c:\\Output.png"));
		// //outputImage.setIcon(new ImageIcon(ImageIO.read(new
		// File("c:\\Output.png"))));

