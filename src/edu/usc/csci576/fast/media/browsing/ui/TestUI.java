/**
 * 
 */
package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Sathish Srinivasan
 * 
 */
public class TestUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int width = 352;
			int height = 288;
			String fileName = String.format("./dataset/image001.rgb");
			Path filePath = Paths.get(fileName);
			TestUI ui = new TestUI();
			byte[] imageBytes = ui.getImageWithFrameNumber(filePath, 0, width,
					height);
			BufferedImage image = ui.getImage(imageBytes, width, height);
			
			int thumbNailWidth =100;
			int thumbNailHeight = 60;
			Image thumbNail = image.getScaledInstance(thumbNailWidth,
					thumbNailHeight, Image.SCALE_SMOOTH);
			BufferedImage thumbNailImage = ui.toBufferedImage(thumbNail);
			ui.display(thumbNailImage, thumbNailWidth, thumbNailHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage toBufferedImage(Image thumbNailImage) {
		if (thumbNailImage instanceof BufferedImage) {
			return (BufferedImage) thumbNailImage;
		}
		BufferedImage bimage = new BufferedImage(thumbNailImage.getWidth(null),
				thumbNailImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(thumbNailImage, 0, 0, null);
		bGr.dispose();
		return bimage;
	}
	
	private void display(BufferedImage thumbNail, int thumbNailWidth,
			int thumbNailHeight) {
			JFrame frame = new JFrame();
			frame.setPreferredSize(new Dimension(352, 288));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JLabel label = null;
			label = new JLabel(new ImageIcon(thumbNail));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
	}

	private BufferedImage getImage(byte[] collagedImageContents, int width,
			int height) {
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
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

	private byte[] getImageWithFrameNumber(Path filePath, int frameNumber,
			int frameWidth, int frameHeight) throws IOException {
		int frameSize = frameWidth * frameHeight * 3;
		byte[] fileContents = new byte[frameSize];
		int startOffset = frameNumber * frameSize;
		FileInputStream fis = new FileInputStream(filePath.toFile());
		fis.read(fileContents, startOffset, frameSize);
		fis.close();
		return fileContents;
	}

}
