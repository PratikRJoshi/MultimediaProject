package edu.usc.csci576.fast.media.browsing.clustering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import edu.usc.csci576.fast.media.browsing.ui.Collage;
import edu.usc.csci576.fast.media.browsing.ui.MainUI;
import edu.usc.csci576.fast.media.browsing.ui.MediaType;

public class VideoHandling {
	
	private static final int framesPerSecond = 30;
	private File videoFile;
	
	public VideoHandling(File file) {
		videoFile = file;
	}
	
	public List<String> getImagesFromVideo() {
		
		List<String> videoFrameAsJPEG = new ArrayList<String>();
		try {
			int numberOfFramesInVideo = MainUI.getNumberOfFrames(videoFile.toPath(), MediaType.Video);
			for (int i = 1; i < numberOfFramesInVideo; i += 30) {
				String fileName = MainUI.getFileNameWithoutExtension(videoFile.toString());
				System.out.println("File name without extension: "+ fileName);
	
				File tempFile = new File(fileName + "_"+ i + ".jpg");
				System.out.println("File to be added = "+ tempFile.getAbsolutePath());
				BufferedImage image = Collage.getImageWithFrameNumber(videoFile.toPath(), i, Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT);
				ImageIO.write(image, "jpg", tempFile);
				System.out.println("Frame name: " + tempFile.getName());
				videoFrameAsJPEG.add(tempFile.toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return videoFrameAsJPEG;
	}
	
	public static List<BufferedImage> getImageWithFrameNumberBetweenTimes(int startSecond, int endSecond, int frameWidth, int frameHeight, Path videoPath) throws IOException{
		List<BufferedImage> listofVideoFrames = new ArrayList<BufferedImage>();
		
		int startFrame = framesPerSecond * startSecond;
		int endFrame = framesPerSecond * endSecond;
		
		for(int i = startFrame; i <= endFrame; i++){
			listofVideoFrames.add(Collage.getImageWithFrameNumber(videoPath, i, frameWidth, frameHeight));
		}
		
		return listofVideoFrames;
	}

	public ImageType classify() {
		List<String> frameAsJPEG = getImagesFromVideo();
		Map<ImageType, Integer> maxImageTypeForFrames = new HashMap<ImageType, Integer>();
		ImageClassification imageClassifier = ImageClassification.getInstance();
		for(String filePath : frameAsJPEG) {
			Path jpegImagePath = Paths.get(filePath);
			ImageType type = imageClassifier.classify(jpegImagePath);
			Integer count = maxImageTypeForFrames.get(type);
			if(count != null){
				count++;
			}else {
				count = new Integer(1);
			}
			maxImageTypeForFrames.put(type, count);
		}
		
		ImageType maxOccuring = null;
		int maxValue = 0;
		for(ImageType currentType : maxImageTypeForFrames.keySet()) {
			int currentValue = maxImageTypeForFrames.get(currentType);
			if(maxOccuring != null){
				maxValue = maxImageTypeForFrames.get(maxOccuring);
			}
			if(currentValue > maxValue) {
				maxOccuring = currentType;
			}
		}
		return maxOccuring;
	}
	
}