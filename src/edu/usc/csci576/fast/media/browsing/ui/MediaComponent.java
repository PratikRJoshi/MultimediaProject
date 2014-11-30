/**
 * 
 */
package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Sathish Srinivasan
 * 
 */
public class MediaComponent extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	private Media media; 
	private HashMap<Rectangle, Media> collageMetadata;
	//private DisplayMedia displayComponent;
	private BufferedImage image;
	//private JLabel label;
	
	public MediaComponent(Media media, DisplayMedia displayComponent) throws IOException {
		//this.displayComponent = displayComponent;
		this.media = media;
		this.collageMetadata = null;
		if(MediaType.Collage == media.getFileType()) {
			readFromMetadataFile();
		}
		//setLayout(new BorderLayout());
		int width = MainUI.getWidth(media.getFileType());
		int height = MainUI.getHeight(media.getFileType());
		Dimension mediaDimension = new Dimension(width, height);
		setMaximumSize(mediaDimension);
		setMinimumSize(mediaDimension);
		addMouseListener((MouseListener) this);
	}

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    if(image != null){
	    Graphics2D g2d = (Graphics2D) g;
	    int x = (this.getWidth() - image.getWidth(null)) / 2;
	    int y = (this.getHeight() - image.getHeight(null)) / 2;
	    g2d.drawImage(image, x, y, this);
	    }
	}
	
	public void displayMedia(Media m) throws IOException {
		//JFrame frame = displayComponent.getFrame();
		Path filePath = m.getFilePath();
		MediaType fileType = m.getFileType();
		int frameWidth = MainUI.getWidth(fileType);
		int frameHeight = MainUI.getHeight(fileType);
		int numOfFrames = getNumberOfFrames(filePath, fileType);
		for(int i=0;i<numOfFrames;i++) {
			image = Collage.getImageWithFrameNumber(filePath, i, frameWidth, frameHeight);
			
			/*//Collage.display(image, frameWidth, frameHeight);
			if(label != null) {
				this.remove(label);
			}
			frame.revalidate();
			frame.repaint();
			this.paintChildren(g);
			ImageIcon icon = new ImageIcon(image);
			label = new JLabel(icon);
			this.add(label);*/
			paintImmediately(0, 0, this.getWidth(), this.getHeight());
			
			/*frame.revalidate();
			frame.repaint();
			frame.pack();
			frame.setVisible(true);*/
			
			try {
				Thread.sleep(1000/30);
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted on sleep");
			}
		}
	}

	private void readFromMetadataFile() throws IOException {
		if (MediaType.Collage == media.getFileType()) {
			String metadataFileName = media.getFilePath().toString()
					+ "_metadata";
			File file = new File(metadataFileName);
			collageMetadata = new HashMap<Rectangle, Media>();
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				Path fileName = Paths.get(tokens[0]);
				MediaType fileType = MediaType.valueOf(tokens[1]);
				Media m = new Media(fileName, fileType);
				int x = Integer.parseInt(tokens[2]);
				int y = Integer.parseInt(tokens[3]);
				int width = Integer.parseInt(tokens[4]);
				int height = Integer.parseInt(tokens[5]);
				Rectangle r = new Rectangle(x, y, width, height);
				collageMetadata.put(r, m);
			}
			bufferedReader.close();
			fileReader.close();
		}
	}

	private int getNumberOfFrames(Path filePath, MediaType fileType) {
		int height = MainUI.getHeight(fileType);
		int width = MainUI.getWidth(fileType);
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
		int height = MainUI.getHeight(mediaType);
		int width = MainUI.getWidth(mediaType);
		int startOffset = height * width * 3 * frameNumber;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			offset =  y * width;
			for (int x = 0; x < width; x++) {
				byte red = fileContents[startOffset + offset + x];
				byte green = fileContents[startOffset + offset + x
						+ (width * height)];
				byte blue = fileContents[startOffset + offset + x
						+ (width * height * 2)];
				int pixelValue = 0xff000000 | ((red & 0xff) << 16)
						| ((green & 0xff) << 8) | (blue & 0xff);
				image.setRGB(x, y, pixelValue);
				offset++;
			}
		}
		if(fileContents.length == 91238400) {
			Collage.display(image, width, height);
		}
		return image;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(MediaType.Collage == media.getFileType()) {
			Point p = new Point(e.getX(), e.getY());
			for(Rectangle r: collageMetadata.keySet()) {
				if(r.contains(p)) {
					//JLabel statusComponent = displayComponent.getStatusComponent();
					Media m = collageMetadata.get(r);
					//statusComponent.setText(m.getFilePath().toString());
					try {
						displayMedia(m);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
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
