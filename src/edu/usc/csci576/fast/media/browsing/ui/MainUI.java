package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;

import edu.usc.csci576.fast.media.browsing.clustering.Constants;
import edu.usc.csci576.fast.media.browsing.clustering.ImageClassification;
import edu.usc.csci576.fast.media.browsing.clustering.ImageType;
import edu.usc.csci576.fast.media.browsing.clustering.VideoHandling;

/**
 * @author Sathish Srinivasan
 */
public class MainUI {

	private static MainUI ui = null;
	private Path mediaFolderName;
	private Path collageFolderName;

	public Path getMediaFolderName() {
		return mediaFolderName;
	}

	public void setMediaFolderName(Path mediaFolderName) {
		this.mediaFolderName = mediaFolderName;
	}

	public Path getCollageFolderName() {
		return collageFolderName.toAbsolutePath();
	}

	public void setCollageFolderName(Path collageFolderName) {
		this.collageFolderName = collageFolderName;
	}

	public static MainUI getInstance() {
		if (ui == null) {
			ui = new MainUI();
		}
		return ui;
	}

	public static int getNumberOfFrames(Path filePath, MediaType fileType) {
		int height = MainUI.getHeight(fileType);
		int width = MainUI.getWidth(fileType);
		int fileSize = (int) filePath.toFile().length();
		
		int sizeOfFrame = height * width * 3;
		int numOfFrames = fileSize / sizeOfFrame;
		return numOfFrames;
	}
	
	
	public static void main(String[] args) throws IOException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		/* Make sure the command line argument is absolute path of the folder*/
		Path mediaFolderPath = Paths.get(args[0]);
		Path collageFolderPath = Paths.get(mediaFolderPath + "/collage");
		//Path jpegFolderPath = Paths.get(mediaFolderPath + "/jpeg");

		MainUI ui = MainUI.getInstance();
		ui.setMediaFolderName(mediaFolderPath);
		ui.createFolderIfNotExists(collageFolderPath);
		ui.setCollageFolderName(collageFolderPath);
		//ui.createFolderIfNotExists(jpegFolderPath);
		
		List<Media> faceList = new ArrayList<Media>();;
		List<Media> cartoonList = new ArrayList<Media>();
		List<Media> buildingList = new ArrayList<Media>();
		List<Media> miscList = new ArrayList<Media>();
		
		deleteAllJPEGFiles(mediaFolderPath);
		ImageClassification imageClassifier = ImageClassification.getInstance();
		File[] files = mediaFolderPath.toFile().listFiles();
		for(File file: files) {
			if(!file.isDirectory()) {
				MediaType fileType = getFileType(file);
				Media m = new Media(file.toPath(), fileType);
				if(fileType == MediaType.Image) {
					Path jpegImagePath = convertRGBToJPEG(file);
					ImageType imgType = imageClassifier.classify(jpegImagePath);
					addToCorrespondingList(m, imgType, faceList, cartoonList, buildingList, miscList);
				} else if(fileType == MediaType.Video) {
					VideoHandling video = new VideoHandling(file);
					ImageType imgType = video.classify();
					addToCorrespondingList(m, imgType, faceList, cartoonList, buildingList, miscList);
				}
			}
		}
		
		Collage faceCollage = new Collage(faceList);
		Collage cartoonCollage = new Collage(cartoonList);
		Collage buildingCollage = new Collage(buildingList);
		Collage miscCollage = imageClassifier.classifyByKMeans(miscList);
		
		List<Media> rootMediaList = new ArrayList<Media>();
		rootMediaList.add(new Media(faceCollage.getCollagedImageFileName(), MediaType.Collage));
		rootMediaList.add(new Media(cartoonCollage.getCollagedImageFileName(), MediaType.Collage));
		rootMediaList.add(new Media(buildingCollage.getCollagedImageFileName(), MediaType.Collage));
		rootMediaList.add(new Media(miscCollage.getCollagedImageFileName(), MediaType.Collage));
		Collage rootCollage = new Collage(rootMediaList);
	
		
		Media display = new Media(rootCollage.getCollagedImageFileName(), MediaType.Collage);
		DisplayMedia image = new DisplayMedia(display);
		image.display();
	}

	public static List<Integer> getListOfRandomNumbers(int max) {
		List<Integer> randomNumbers = new ArrayList<Integer>();
		for(int i=0;i<max;i++) {
			randomNumbers.add(i);
		}
		Collections.shuffle(randomNumbers);
		return randomNumbers;
	}
	
	public static void deleteAllJPEGFiles(Path dir) {
		File[] files = dir.toFile().listFiles();
		for(File file: files) {
			if(!file.isDirectory() && file.getName().endsWith(".jpg")) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void addToCorrespondingList(Media m, ImageType imgType,
			List<Media> faceList, List<Media> cartoonList,
			List<Media> buildingList, List<Media> miscList) {
		if(imgType == ImageType.FACE) {
			faceList.add(m);
		} else if(imgType == ImageType.CARTOONS) {
			cartoonList.add(m);
		} else if(imgType == ImageType.BUILDINGS) {
			buildingList.add(m);
		} else {
			miscList.add(m);
		}
	}

	public static Path convertRGBToJPEG(File fileEntry) {
		try {
			MediaType type = getFileType(fileEntry); 
			if (type != MediaType.Video && fileEntry.toString().endsWith(".rgb")) {
				String outputFileName = getFileNameWithoutExtension(fileEntry.toString()) + ".jpg";
				File outputFile = new File(outputFileName);
				Files.deleteIfExists(outputFile.toPath());
				
				BufferedImage image = null;
				if(type == MediaType.Image) {
					image = Collage.getImageWithFrameNumber(fileEntry.toPath(), 0,
							Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT);
				} else if(type == MediaType.Collage) {
					image = Collage.getImageWithFrameNumber(fileEntry.toPath(), 0,
							Constants.COLLAGED_IMAGE_WIDTH, Constants.COLLAGED_IMAGE_HEIGHT);
					Image scaledImage = image.getScaledInstance(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
					image = Collage.toBufferedImage(scaledImage);
				}
				ImageIO.write(image, "jpg", outputFile);
				return outputFile.toPath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFileNameWithoutExtension(String imagePath) {
		String[] tokens = imagePath.split("\\.");
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<tokens.length-1;i++) {
			builder.append(tokens[0]);
		}
		return builder.toString();
	}
	
	public static MediaType getFileType(File fileEntry) {
		if(fileEntry.length() == Constants.IMAGE_LENGTH) {
			return MediaType.Image;
		} else if(fileEntry.length() == Constants.COLLAGE_LENGTH) {
			return MediaType.Collage;
		} else {
			return MediaType.Video;
		}
	}
	
	public static int getWidth(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return Constants.IMAGE_WIDTH;
		} else {
			return Constants.COLLAGED_IMAGE_WIDTH;
		}
	}

	public static int getHeight(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return Constants.IMAGE_HEIGHT;
		} else {
			return Constants.COLLAGED_IMAGE_HEIGHT;
		}
	}
	
	/* Path of the folder to be created should be absolute Path */ 
	public void createFolderIfNotExists(Path folderPath) throws IOException {
		if (!Files.exists(folderPath)) {
			Files.createDirectory(folderPath);
		}
	}
}
