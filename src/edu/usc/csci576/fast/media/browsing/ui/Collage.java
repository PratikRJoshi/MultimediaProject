package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
	
	public Collage(List<Media> mediaList) throws IOException {
		Dimension d = getThumbNailDimensions(mediaList.size());
		createCollage(mediaList, d.width, d.height);
	}

	private Dimension getThumbNailDimensions(int listSize) {
		int nearestBiggerSize = getNearestBiggerSize(listSize);
		int thumbNailSize = COLLAGED_IMAGE_WIDTH * COLLAGED_IMAGE_HEIGHT / nearestBiggerSize;
		List<Integer> divisors = getDivisors(thumbNailSize);
		Collections.sort(divisors);
		int index = divisors.size() / 2;
		int thumNailWidth = divisors.get(index);
		int thumNailHeight = divisors.get(index-1);
		return new Dimension(thumNailWidth, thumNailHeight);
	}
	
	private List<Integer> getDivisors(int thumbNailSize) {
		List<Integer> divisors = new ArrayList<Integer>();
		int max = (int)Math.floor(Math.sqrt(thumbNailSize));
		for (int i = 1; i <= max; i++) {
			if (thumbNailSize % i == 0) {
				int divisor = thumbNailSize / i;
				if(COLLAGED_IMAGE_WIDTH % i == 0 && COLLAGED_IMAGE_WIDTH % divisor == 0) {
					divisors.add(i);
					divisors.add(thumbNailSize / i);
				}
			}
		}
		return divisors;
	}

	private int getNearestBiggerSize(int listSize) {
		int[] roundListSizes = {1,4,6,8,9,10,12,15,16,18,20,24,25,30,32,36,40,45,50,60};
		for(int i=0;i<roundListSizes.length;i++) {
			if(listSize <= roundListSizes[i]) {
				return roundListSizes[i];
			}
		}
		return roundListSizes[roundListSizes.length-1];
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
			String contents = String.format("%s %s %d %d %d %d\n", media
					.getFilePath().toString(), media.getFileType().name(),
					startX, startY, thumbNailWidth, thumbNailHeight);
			metadataFileStream.write(contents.getBytes());
			count++;
		}
		metadataFileStream.close();
		writeToCollageFile(collagedImageContents);
	}

	private void copyToCollage(byte[] collagedImageContents,int startY, int startX,
			byte[] thumbNailContents, int thumbNailWidth, int thumbNailHeight) {
		int orgLength = thumbNailHeight * thumbNailWidth;
		int mappedLength = COLLAGED_IMAGE_HEIGHT * COLLAGED_IMAGE_WIDTH;
		for(int y=0;y<(thumbNailHeight-2);y++) {
			int mappedY = (startY + y)*COLLAGED_IMAGE_WIDTH;
			int orgIndex = y * thumbNailWidth;
			int mappedIndex = mappedY+startX;
			for(int x=0;x<(thumbNailWidth-2);x++){
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
		//display(image, width, height);
	}

	private byte[] createThumbNail(Media media, int thumbNailWidth, int thumbNailHeight) throws IOException {
		BufferedImage image = preProcess(media);
		BufferedImage thumbNailImage = toBufferedImage(image.getScaledInstance(thumbNailWidth, thumbNailHeight, Image.SCALE_SMOOTH));
		
		byte[] thumbNailBytes = new byte[thumbNailHeight * thumbNailWidth * 3];
		int offset = 0;
		for(int y=0;y<thumbNailHeight;y++) {
			for(int x=0;x<thumbNailWidth;x++) {
				int pixelValue= thumbNailImage.getRGB(x, y);
				Color c = new Color(pixelValue);
				thumbNailBytes[offset] = (byte) c.getRed();
				thumbNailBytes[offset + (thumbNailHeight * thumbNailWidth)] = (byte) c.getGreen();
				thumbNailBytes[offset + (thumbNailHeight * thumbNailWidth * 2)] = (byte) c.getBlue();
				offset++;
			}
		}
		return thumbNailBytes;
	}

	private BufferedImage toBufferedImage(Image thumbNailImage) {
		if (thumbNailImage instanceof BufferedImage) {
			return (BufferedImage) thumbNailImage;
		}
		BufferedImage bimage = new BufferedImage(thumbNailImage.getWidth(null),
				thumbNailImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(thumbNailImage, 0, 0, null);
		bGr.dispose();
		return bimage;
	}

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

	private BufferedImage preProcess(Media media) throws IOException {
		Path filePath = media.getFilePath();
		int width = MainUI.getWidth(media.getFileType());
		int height = MainUI.getHeight(media.getFileType());
		int frameNumber = 0;
		long fileSize = filePath.toFile().length();
		int sizeOfSingleFrame = width * height * 3;
		int numOfFrames = (int) (fileSize / sizeOfSingleFrame);
		if (MediaType.Video == media.getFileType()) {
			Random rand = new Random();
			frameNumber = rand.nextInt(numOfFrames);
		}
		BufferedImage orgImage = getImageWithFrameNumber(filePath, frameNumber, width, height);
		//display(orgImage, width, height);
		return orgImage;
	}

	public static void display(BufferedImage image, int width, int height) {
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(352, 288));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JLabel label = null;
		label = new JLabel(new ImageIcon(image));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	/*private void display(Path filePath, int width, int height) throws IOException {
		byte[] filecontents = new byte[width * height * 3];
		FileInputStream fis = new FileInputStream(filePath.toFile());
		fis.read(filecontents);
		fis.close();
		display(filecontents, width, height);
	}*/
	
	public static BufferedImage getImage(byte[] collagedImageContents, int width, int height) {
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

	public static BufferedImage getImageWithFrameNumber(Path filePath,
			int frameNumber, int frameWidth, int frameHeight)
			throws IOException {
		int frameSize = frameWidth * frameHeight;
		byte[] fileContents = new byte[frameSize * 3];
		int startOffset = frameNumber * frameSize * 3;
		RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
		raf.seek(startOffset);
		raf.read(fileContents);
		raf.close();
		
		BufferedImage image = getImage(fileContents, frameWidth, frameHeight);
		return image;
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
