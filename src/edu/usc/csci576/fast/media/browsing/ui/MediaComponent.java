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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.swing.JComponent;

/**
 * @author Sathish Srinivasan
 * 
 */
public class MediaComponent extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	private Media media; 
	private HashMap<Rectangle, Media> collageMetadata;
	private BufferedImage image;
	
	public MediaComponent(Media media, DisplayMedia displayComponent) throws IOException {
		//this.displayComponent = displayComponent;
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
		this.media = m;
		this.collageMetadata = null;
		if(MediaType.Collage == m.getFileType()) {
			readFromMetadataFile();
		}
		Path filePath = m.getFilePath();
		MediaType fileType = m.getFileType();
		int frameWidth = MainUI.getWidth(fileType);
		int frameHeight = MainUI.getHeight(fileType);
		int numOfFrames = getNumberOfFrames(filePath, fileType);
		for(int i=0;i<numOfFrames;i++) {
			image = Collage.getImageWithFrameNumber(filePath, i, frameWidth, frameHeight);
			paintImmediately(0, 0, this.getWidth(), this.getHeight());			
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if(MediaType.Collage == media.getFileType()) {
			Point p = new Point(e.getX(), e.getY());
			System.out.println(p.toString());
			for(Rectangle r: collageMetadata.keySet()) {
				if(r.contains(p)) {
					Media m = collageMetadata.get(r);
					try {
						this.media = m;
						displayMedia(m);
					} catch (IOException e1) {
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
