package edu.usc.csci576.fast.media.browsing.clustering;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.usc.csci576.fast.media.browsing.ui.Collage;
import edu.usc.csci576.fast.media.browsing.ui.MainUI;
import edu.usc.csci576.fast.media.browsing.ui.Media;
import edu.usc.csci576.fast.media.browsing.ui.MediaType;

/**
 * @author Sathish Srinivasan
 */
public class ImageClassification {

	private static ImageClassification imageClassifier;
	
	public static ImageClassification getInstance() {
		if (imageClassifier == null) {
			imageClassifier = new ImageClassification();
		}
		return imageClassifier;
	}
	
	public ImageType classify(Path jpegImagePath) {
		if(hasFace(jpegImagePath)) {
			if(hasCartoons(jpegImagePath)) {
				return ImageType.CARTOONS;
			} else {
				return ImageType.FACE;
			}
		} else if(hasCartoons(jpegImagePath)) {
			return ImageType.CARTOONS;
		} else if(hasBuildings(jpegImagePath)) {
			return ImageType.BUILDINGS;
		} else {
			return ImageType.MISC;
		}
	}

	private boolean hasBuildings(Path jpegImagePath) {
		BuildingsDetector detector = new BuildingsDetector();
		return detector.hasBuildings(jpegImagePath);
	}

	private boolean hasCartoons(Path jpegImagePath) {
		CartoonDetector detector = new CartoonDetector();
		return detector.hasCartoons(jpegImagePath);
	}

	private boolean hasFace(Path jpegImagePath) {
		FaceDetector detector = new FaceDetector();
		return detector.hasFace(jpegImagePath);
	}

	public Collage classifyByKMeans(List<Media> miscList) {
		try {
			Map<Integer, List<Media>> clusterMap = clusterIntoKBuckets(miscList);
			List<Media> rootMiscCollageList = new ArrayList<Media>();
			for (Integer key : clusterMap.keySet()) {
				List<Media> mediaList = clusterMap.get(key);
				Collage c = new Collage(mediaList);
				rootMiscCollageList.add(new Media(c.getCollagedImageFileName(),
						MediaType.Collage));
			}
			return new Collage(rootMiscCollageList);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<Integer, List<Media>> clusterIntoKBuckets(List<Media> miscList) {
		
		int numberOfBaseImages = 9;
		double meanArr[] = new double[numberOfBaseImages];

		Map<Integer, List<Media>> collageList = new HashMap<Integer, List<Media>>();

		List<Integer> randomNumbers = MainUI.getListOfRandomNumbers(miscList.size());
		List<Mat> yuvBaseRefList = new ArrayList<Mat>();
		int count = 0;
		for (Integer i : randomNumbers) {
			Media m = miscList.get(i);
			if(m.getFileType() != MediaType.Video) {
				Path jpegImagePath = MainUI.convertRGBToJPEG(m.getFilePath().toFile());
				Mat baseRef = Highgui.imread(jpegImagePath.toString());
				Mat yuvBaseRef = new Mat();
				Imgproc.cvtColor(baseRef, yuvBaseRef, Imgproc.COLOR_RGB2YUV);
				yuvBaseRefList.add(yuvBaseRef);
				count++;
			}
			if(count >= numberOfBaseImages) {
				break;
			}
		}

		for(Media m : miscList){
			if(m.getFileType() != MediaType.Video) {
				Path jpegImagePath = MainUI.convertRGBToJPEG(m.getFilePath().toFile());
				Mat image = Highgui.imread(jpegImagePath.toString());
				Mat yuvimg = new Mat();
				Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2YUV);

				for (int i = 0; i < numberOfBaseImages; i++) {
					double tempVal = 0;
					for (int j = 0; j < Constants.IMAGE_HEIGHT; j++) {
						for (int k = 0; k < Constants.IMAGE_WIDTH; k++) {
							double yuvPixelDiff = (yuvBaseRefList.get(i).get(j, k)[0]) - (yuvimg.get(j, k)[0]);
							tempVal += Math.pow(yuvPixelDiff, 2);
						}
					}
					meanArr[i] = Math.sqrt(tempVal);
				}
				int index = findMin(meanArr);
				System.out.println("Inserting into index " + index);
				
				if (collageList.get(index) == null) {
					List<Media> tempList = new ArrayList<Media>();
					tempList.add(m);
					collageList.put(index, tempList);
				} else {
					collageList.get(index).add(m);
				}
			}
		}
		
		/*Add all videos as another cluster*/
		List<Media> miscVideoList = new ArrayList<Media>();
		for(Media m : miscList){
			if(m.getFileType() == MediaType.Video) {
				miscVideoList.add(m);
			}
		}
		if(!miscVideoList.isEmpty()) {
			collageList.put(numberOfBaseImages, miscVideoList);
		}
		return collageList;
		
	}
	
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
}
