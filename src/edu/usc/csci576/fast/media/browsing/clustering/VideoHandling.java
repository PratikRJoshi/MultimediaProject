package edu.usc.csci576.fast.media.browsing.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.usc.csci576.fast.media.browsing.ui.Collage;
import edu.usc.csci576.fast.media.browsing.ui.MainUI;
import edu.usc.csci576.fast.media.browsing.ui.Media;
import edu.usc.csci576.fast.media.browsing.ui.MediaComponent;
import edu.usc.csci576.fast.media.browsing.ui.MediaType;

public class VideoHandling {
	
	static final int framesPerSecond = 30;
	
	public static List<String> getImagesFromVideos(String args, Path rootFolder) throws IOException, InterruptedException{
		Path mediaFolderPath = Paths.get(args);
		List<List<BufferedImage>> listOfVideoFrames = new ArrayList<List<BufferedImage>>();
		List<Integer> randomImageNumbers = ImageClassifier.getListOfRandomNumbers(300);
		List<BufferedImage> tempList;
		List<String> videoFilePaths = new ArrayList<String>();
		File videoDir = new File("C:/video");
		
			videoDir.mkdir();
		for (File fileEntry : mediaFolderPath.toFile().listFiles()) {
			if((!fileEntry.isDirectory()) && (ImageClassifier.getFileType(fileEntry) == MediaType.Video)){
				tempList = new ArrayList<BufferedImage>();
				Path videoPath = Paths.get(fileEntry.getAbsolutePath());
				// System.out.println("Path: "+videoPath.getFileName());
				Media video = new Media(videoPath, MediaType.Video);
				MediaType fileType = video.getFileType();
				int frameWidth = MainUI.getWidth(fileType);
				int frameHeight = MainUI.getHeight(fileType);
				int numberOfFramesInVideo = MediaComponent.getNumberOfFrames(
						videoPath, fileType);
				// System.out.println("Number of frames in this video = "+numberOfFramesInVideo);

				// System.out.println(fileEntry.getName()+"frame width = "+frameWidth);
				// System.out.println(fileEntry.getName()+"frame height = "+frameHeight);

				for (int i = 1; i < numberOfFramesInVideo; i += 30) {
					String fileName = ImageClassifier.getFileNameWithoutExtension(videoPath.getFileName().toString());
					System.out.println("File name without extension: "+ fileName);

					File tempFile = new File(rootFolder+"/"+ fileName + "_"+ i + ".jpg");
					System.out.println("File to be added = "+ tempFile.getAbsolutePath());
					// tempList.add(Collage.getImageWithFrameNumber(videoPath,
					// randomImageNumbers.get(i), frameWidth, frameHeight) );
					BufferedImage image = Collage.getImageWithFrameNumber(videoPath, randomImageNumbers.get(i), frameWidth,frameHeight);
					ImageIO.write(image, "jpg", tempFile);
					System.out.println("Frame name: " + tempFile.getName());
					videoFilePaths.add(tempFile.getName());
				}
				listOfVideoFrames.add(tempList);
				// System.out.println("Video finished");
				Thread.sleep((1000 / framesPerSecond) + 332);
			}

			// JFrame frame = new JFrame();
			// frame.setPreferredSize(new Dimension(352, 288));
			// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// JLabel label = null;
			// int k = 1;
			// for(int i = 0; i < listOfVideoFrames.size(); i++){
			// for(int j = 0; j < listOfVideoFrames.get(i).size(); j++){
			// // System.out.println(k++);
			//
			// label = new JLabel(new
			// ImageIcon(listOfVideoFrames.get(i).get(j)));
			// frame.getContentPane().add(label, BorderLayout.CENTER);
			// frame.pack();
			// frame.setVisible(true);
			// Thread.sleep((1000/framesPerSecond)+ 332);
			// frame.remove(label);
			// }
			// }
		}
			System.out.println("Done");
			return videoFilePaths;
		
//		int startSecond = 4;
//		int endSecond = 6;
//		
//		List<BufferedImage> framesFromVideos = getImageWithFrameNumberBetweenTimes( startSecond,  endSecond,  frameWidth,  frameHeight,  videoPath);
//		startSecond = 4;
//		endSecond = 6;
		
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
	
}
