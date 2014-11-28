package edu.usc.csci576.fast.media.browsing.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

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
		int destPos = 0;
		int count = 1;
		for (Media media : mediaList) {
			byte[] thumbNailBytes = createThumbNail(media, thumbNailWidth, thumbNailHeight);
			System.arraycopy(thumbNailBytes, 0, collagedImageContents, destPos, thumbNailBytes.length);
			destPos += thumbNailBytes.length;
			int numOfThumNailPerRow = COLLAGED_IMAGE_WIDTH / thumbNailWidth;
			int startY = ((count-1) / numOfThumNailPerRow) * thumbNailHeight;
			int startX = ((count-1) % numOfThumNailPerRow) * thumbNailWidth;
			String contents = String.format("%s %d %d %d %d\n", media.getFilePath().toString(), startX, startY, thumbNailWidth, thumbNailHeight);
			metadataFileStream.write(contents.getBytes());
			count++;
		}
		metadataFileStream.close();
		writeToCollageFile(collagedImageContents);
	}

	private byte[] createThumbNail(Media media, int thumbNailWidth, int thumbNailHeight) throws IOException {
		int mediaWidth = MainUI.getWidth(media.getFileType());
		int mediaHeight = MainUI.getHeight(media.getFileType());
		byte[] mediaContents = preProcess(media);
		byte[] thumbNailImage = new byte[thumbNailWidth * thumbNailHeight * 3];
		//BufferedImage thumbNail = new BufferedImage(thumbNailWidth, thumbNailHeight, BufferedImage.TYPE_INT_RGB);
		int thumbNailOffset = 0;
		int mediaOffset = 0;
		float verScalingFactor = (float) mediaHeight / thumbNailHeight;
		float horScalingFactor = (float) mediaWidth / thumbNailWidth;
		for (int y = 0; y < thumbNailHeight; y++) {
			for (int x = 0; x < thumbNailWidth; x++) {
				mediaOffset = (int) ((y * verScalingFactor) * mediaWidth + (x * horScalingFactor));
				thumbNailImage[thumbNailOffset] = mediaContents[mediaOffset];
				thumbNailImage[thumbNailOffset + (thumbNailWidth * thumbNailHeight)] = mediaContents[mediaOffset + (mediaWidth * mediaHeight)];
				thumbNailImage[thumbNailOffset + (thumbNailWidth * thumbNailHeight*2)] = mediaContents[mediaOffset + (mediaWidth * mediaHeight * 2)];
				thumbNailOffset++;
			}
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
		return getImageWithFrameNumber(filePath, frameNumber, width, height);
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

	/*private BufferedImage getImage(byte[] collagedImageContents) {
		BufferedImage image = new BufferedImage(COLLAGED_IMAGE_WIDTH, COLLAGED_IMAGE_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		int offset = 0;
		for (int y = 0; y < COLLAGED_IMAGE_HEIGHT; y++) {
			for (int x = 0; x < COLLAGED_IMAGE_WIDTH; x++) {
				byte red = collagedImageContents[offset];
				byte green = collagedImageContents[offset + COLLAGED_IMAGE_WIDTH * COLLAGED_IMAGE_HEIGHT];
				byte blue = collagedImageContents[offset + COLLAGED_IMAGE_WIDTH * COLLAGED_IMAGE_HEIGHT * 2];
				int pixelValue = 0xff000000 | ((red & 0xff) << 16)
						| ((green & 0xff) << 8) | (blue & 0xff);
				image.setRGB(x, y, pixelValue);
				offset++;
			}
		}
		return image;
	}*/

}
