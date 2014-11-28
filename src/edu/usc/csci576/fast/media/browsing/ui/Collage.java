package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Sathish Srinivasan
 */
public class Collage {

	public static final int COLLAGED_IMAGE_WIDTH = 600;
	public static final int COLLAGED_IMAGE_HEIGHT = 600;
	private static int uniqueId = 1;
	private Path collagedImageFileName;
	private Path collagedImageMetadataFileName;

	public Path getCollagedImageFileName() {
		return collagedImageFileName;
	}
	
	public Path getCollagedImageMetadataFileName() {
		return collagedImageMetadataFileName;
	}
	
	public Collage(List<Media> mediaList, int thumbNailWidth,
			int thumbNailHeight) throws IOException {
		createCollage(mediaList, thumbNailWidth, thumbNailHeight);
	}

	private void createCollage(List<Media> mediaList, int thumbNailWidth,
			int thumbNailHeight) throws IOException {
		byte[] collagedImageContents = new byte[COLLAGED_IMAGE_WIDTH
		                        				* COLLAGED_IMAGE_HEIGHT * 3];
		collagedImageFileName = createCollageFile();
		collagedImageMetadataFileName = createCollageMetadataFile(collagedImageFileName);
		OutputStream metadataFileStream = new FileOutputStream(collagedImageMetadataFileName.toFile());
		//String contents = String.valueOf(thumbNailWidth) + " " + String.valueOf(thumbNailHeight)+"\n";
		//metadataFileStream.write(contents.getBytes());
		int count = 1;
		for (Media media : mediaList) {
			byte[] thumbNailBytes = createThumbNail(media, thumbNailWidth, thumbNailHeight);
			display(thumbNailBytes, thumbNailWidth, thumbNailHeight);
			int numOfThumNailPerRow = COLLAGED_IMAGE_WIDTH / thumbNailWidth;
			int startY = ((count-1) / numOfThumNailPerRow) * thumbNailHeight;
			int startX = ((count-1) % numOfThumNailPerRow) * thumbNailWidth;
			copyToCollage(collagedImageContents, startY, startX, thumbNailBytes, thumbNailWidth, thumbNailHeight);
			String contents = String.format("%s %d %d %d %d\n", media.getFilePath().toString(), startX, startY, thumbNailWidth, thumbNailHeight);
			metadataFileStream.write(contents.getBytes());
			count++;
		}
		metadataFileStream.close();
		writeToCollageFile(collagedImageContents);
	}

	private void copyToCollage(byte[] collagedImageContents,int startY, int startX,
			byte[] thumbNailContents, int thumbNailWidth, int thumbNailHeight) {
		int mappedX = startX * thumbNailWidth;
		int orgLength = thumbNailHeight * thumbNailWidth;
		int mappedLength = COLLAGED_IMAGE_HEIGHT * COLLAGED_IMAGE_WIDTH;
		for(int y=0;y<thumbNailHeight;y++) {
			int mappedY = (startY + y)*COLLAGED_IMAGE_WIDTH;
			int orgIndex = y * thumbNailWidth;
			int mappedIndex = mappedY+mappedX;
			for(int x=0;x<thumbNailWidth;x++){
				collagedImageContents[mappedIndex] = thumbNailContents[orgIndex];
				collagedImageContents[mappedIndex+mappedLength] = thumbNailContents[orgIndex + orgLength];
				collagedImageContents[mappedIndex+(mappedLength * 2)] = thumbNailContents[orgIndex + (orgLength*2)];
				mappedIndex++;
				orgIndex++;
			}
		}
		return;
	}

	private void display(byte[] imageBytes, int width, int height) {
		BufferedImage image = getImage(imageBytes, width, height);
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(352, 288));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel label = null;
		label = new JLabel(new ImageIcon(image));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private byte[] createThumbNail(Media media, int thumbNailWidth, int thumbNailHeight) throws IOException {
		int mediaWidth = MainUI.getWidth(media.getFileType());
		int mediaHeight = MainUI.getHeight(media.getFileType());
		byte[] mediaContents = preProcess(media);
		byte[] thumbNailImage = new byte[thumbNailWidth * thumbNailHeight * 3];
		//BufferedImage thumbNail = new BufferedImage(thumbNailWidth, thumbNailHeight, BufferedImage.TYPE_INT_RGB);
		int thumbNailLength = thumbNailWidth * thumbNailHeight;
		int mediaLength = mediaWidth * mediaHeight;
		int scalingFactor = mediaLength / thumbNailLength;
		int mappedIndex = 0;
		for (int i = 0; i < thumbNailLength; i++) {
			mappedIndex = (int)(i* scalingFactor);
			thumbNailImage[i] =  mediaContents[mappedIndex];
			thumbNailImage[i+thumbNailLength] = mediaContents[mappedIndex + mediaLength];
			thumbNailImage[i+(thumbNailLength*2)] = mediaContents[mappedIndex + (mediaLength * 2)];
		}
		return thumbNailImage;
	}

	/*private BufferedImage toBufferedImage(Image thumbNailImage) {
		if (thumbNailImage instanceof BufferedImage) {
			return (BufferedImage) thumbNailImage;
		}
		BufferedImage bimage = new BufferedImage(thumbNailImage.getWidth(null),
				thumbNailImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(thumbNailImage, 0, 0, null);
		bGr.dispose();
		return bimage;
	}*/

	private void writeToCollageFile(byte[] collagedImageContents) throws IOException {
		OutputStream collagedImageStream = new FileOutputStream(collagedImageFileName.toFile());
		collagedImageStream.write(collagedImageContents);
		collagedImageStream.close();
	}

	/*private void populateContentsOfCollage(byte[] collagedImageContents, Path filePath, byte[] thumbNailBytes, int count,
			int thumbNailWidth, int thumbNailHeight, OutputStream metadataFileStream) throws IOException {
		int startOffset = (thumbNailWidth * thumbNailHeight * 3) * (count - 1);
		int offset = 0;
		Color c = null;
		for (int y = 0; y < thumbNailHeight; y++) {
			for (int x = 0; x < thumbNailWidth; x++) {
				c = new Color(thumbNail.getRGB(x, y));
				collagedImageContents[startOffset+offset] = (byte) c.getRed();
				collagedImageContents[startOffset+offset+thumbNailWidth] = (byte) c.getGreen();
				collagedImageContents[startOffset+offset+(thumbNailWidth*2)] = (byte) c.getBlue();
				offset++;
			}
		}
		int numOfThumNailPerRow = COLLAGED_IMAGE_WIDTH / thumbNailWidth;
		int startY = ((count-1) / numOfThumNailPerRow) * thumbNailHeight;
		int startX = ((count-1) % numOfThumNailPerRow) * thumbNailWidth;
		String contents = String.format("%s %d %d %d %d\n", filePath.toString(), startX, startY, thumbNailWidth, thumbNailHeight);
		metadataFileStream.write(contents.getBytes());
		metadataFileStream.flush();
	}*/

	private byte[] preProcess(Media media) throws IOException {
		Path filePath = media.getFilePath();
		int width = MainUI.getWidth(media.getFileType());
		int height = MainUI.getHeight(media.getFileType());
		int frameNumber = 0;
		if (MediaType.Video == media.getFileType()) {
			long fileSize = filePath.toFile().length();
			int sizeOfSingleFrame = width * height * 3;
			int numOfFrames = (int) (fileSize / sizeOfSingleFrame);
			Random rand = new Random();
			frameNumber = rand.nextInt(numOfFrames);
		}
		display(filePath, width, height);
		return getImageWithFrameNumber(filePath, frameNumber, width, height);
	}

	private void display(Path filePath, int width, int height) throws IOException {
		byte[] filecontents = new byte[width * height * 3];
		FileInputStream fis = new FileInputStream(filePath.toFile());
		fis.read(filecontents);
		fis.close();
		display(filecontents, width, height);
	}
	
	private BufferedImage getImage(byte[] collagedImageContents, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				byte red = collagedImageContents[offset];
				byte green = collagedImageContents[offset + width * height];
				byte blue = collagedImageContents[offset + width * height * 2];
				int pixelValue = 0xff000000 | ((red & 0xff) << 16)
						| ((green & 0xff) << 8) | (blue & 0xff);
				image.setRGB(x, y, pixelValue);
				offset++;
			}
		}
		return image;
	}

	private byte[] getImageWithFrameNumber(Path filePath,
			int frameNumber, int frameWidth, int frameHeight)
			throws IOException {
		int frameSize = frameWidth * frameHeight * 3;
		byte[] fileContents = new byte[frameSize];
		int startOffset = frameNumber * frameSize;
		FileInputStream fis = new FileInputStream(filePath.toFile());
		fis.read(fileContents, startOffset, frameSize);
		fis.close();
		return fileContents;
	}

	private Path createCollageMetadataFile(Path collageFileName) throws IOException {
		String collageMetadataFileName = collageFileName + "_metadata";
		File file = new File(collageMetadataFileName);
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		return file.toPath();
	}

	private Path createCollageFile() throws IOException {
		MainUI ui = MainUI.getInstance();
		String collageFileName = ui.getCollageFolderName()
				.resolve(Paths.get("collage_" + Collage.uniqueId + ".rgb"))
				.toString();
		Collage.uniqueId++;
		File file = new File(collageFileName);
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		return file.toPath();
	}

}
