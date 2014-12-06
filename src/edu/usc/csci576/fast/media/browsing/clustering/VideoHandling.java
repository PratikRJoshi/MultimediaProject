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

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.usc.csci576.fast.media.browsing.ui.Collage;
import edu.usc.csci576.fast.media.browsing.ui.MainUI;
import edu.usc.csci576.fast.media.browsing.ui.Media;
import edu.usc.csci576.fast.media.browsing.ui.MediaType;

public class VideoHandling {
	
	static final int framesPerSecond = 30;
	
	public static void main(String args[]) throws IOException, InterruptedException{
		Path mediaFolderPath = Paths.get(args[0]);
		List<List<BufferedImage>> listOfVideoFrames = new ArrayList<List<BufferedImage>>();
		List<Integer> randomImageNumbers = ImageClassifier.getListOfRandomNumbers();
		List<BufferedImage> tempList;
		for (File fileEntry : mediaFolderPath.toFile().listFiles()) {
			tempList = new ArrayList<BufferedImage>();
			Path videoPath = Paths.get(fileEntry.getAbsolutePath());
			System.out.println("Path: "+videoPath.getFileName());
			Media video = new Media(videoPath, MediaType.Video);
			MediaType fileType = video.getFileType();
			int frameWidth = MainUI.getWidth(fileType);
			int frameHeight = MainUI.getHeight(fileType);
			System.out.println(fileEntry.getName()+"frame width = "+frameWidth);
			System.out.println(fileEntry.getName()+"frame height = "+frameHeight);
			
			for(int i = 0; i < 300; i++){
				System.out.println("Random number = "+randomImageNumbers.get(i));
				tempList.add(Collage.getImageWithFrameNumber(videoPath, randomImageNumbers.get(i), frameWidth, frameHeight) );
			}
			listOfVideoFrames.add(tempList);
			
		}
		
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(352, 288));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JLabel label = null;
		int k = 1;
		for(int i = 0; i < listOfVideoFrames.size(); i++){
			for(int j = 0; j < listOfVideoFrames.get(i).size(); j++){
				System.out.println(k++);
				
				label = new JLabel(new ImageIcon(listOfVideoFrames.get(i).get(j)));
				frame.getContentPane().add(label, BorderLayout.CENTER);
				frame.pack();
				frame.setVisible(true);
				Thread.sleep((1000/framesPerSecond)- 10);
				frame.remove(label);
			}
		}
		
		System.out.println("Done");
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
