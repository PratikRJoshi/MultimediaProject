/**
 * 
 */
package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * @author Sathish Srinivasan
 * 
 */
public class MediaComponent extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	JLabel statusComponent;
	Media media;

	public MediaComponent(Media media, JLabel statusComponent) throws IOException {
		this.statusComponent = statusComponent;
		this.media = media;
		setLayout(new BorderLayout());
		int width = getWidth(media.getFileType());
		int height = getHeight(media.getFileType());
		Dimension mediaDimension = new Dimension(width, height);
		setMaximumSize(mediaDimension);
		setMinimumSize(mediaDimension);
	}

	public void displayMedia() throws IOException {
		Path filePath = media.getFilePath();
		MediaType fileType = media.getFileType();
		byte[] fileContents = readFromFile(filePath);
		BufferedImage image = null;
		JLabel label = null;
		int numOfFrames = getNumberOfFrames(filePath, fileType);
		 int count = 0;
		for(int i=0;i<numOfFrames;i++) {
			image = createImage(fileContents, i, fileType);
			if(label != null) {
				remove(label);
			}
			if(count == 30) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted on sleep");
				}
				count = 0;
			}
			label = new JLabel(new ImageIcon(image));
			add(label);
			count++;
			System.out.println("processing frame " + count);
		}
		addMouseListener((MouseListener) this);
	}

	private int getNumberOfFrames(Path filePath, MediaType fileType) {
		int height = getHeight(fileType);
		int width = getWidth(fileType);
		int fileSize = (int) filePath.toFile().length();
		
		int sizeOfFrame = height * width * 3;
		int numOfFrames = fileSize / sizeOfFrame;
		return numOfFrames;
	}

	private byte[] readFromFile(Path filePath) throws IOException {
		int fileSize = (int) filePath.toFile().length();
		byte[] fileContents = new byte[fileSize];
		FileInputStream fis = new FileInputStream(filePath.toFile());
		fis.read(fileContents);
		fis.close();
		return fileContents;
	}

	private BufferedImage createImage(byte[] fileContents, int frameNumber,
			MediaType mediaType) throws IOException {

		int offset = 0;
		int height = getHeight(mediaType);
		int width = getWidth(mediaType);
		int startOffset = height * width * 3 * frameNumber;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				byte red = fileContents[startOffset + offset];
				byte green = fileContents[startOffset + offset
						+ (width * height)];
				byte blue = fileContents[startOffset + offset
						+ (width * height * 2)];
				int pixelValue = 0xff000000 | ((red & 0xff) << 16)
						| ((green & 0xff) << 8) | (blue & 0xff);
				image.setRGB(x, y, pixelValue);
				offset++;
			}
		}
		return image;
	}

	private int getWidth(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return 352;
		} else {
			return Collage.COLLAGED_IMAGE_WIDTH;
		}
	}

	private int getHeight(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return 288;
		} else {
			return Collage.COLLAGED_IMAGE_HEIGHT;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x, y;
		x = e.getX();
		y = e.getY();
		String status = "x = " + x + " y = " + y;
		statusComponent.setText(status);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
