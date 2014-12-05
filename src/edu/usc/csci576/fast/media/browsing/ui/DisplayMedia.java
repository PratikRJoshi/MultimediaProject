package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

/**
 * @author Sathish Srinivasan
 */
public class DisplayMedia implements MouseListener{
	private Media media;
	private JFrame frame;
	private JPanel menuComponent;
	private JLabel statusComponent;
	private Button backButton;
	private JPanel contentComponent;
	private MediaComponent mediaComponent;

	public JFrame getFrame() {
		return frame;
	}
	
	public JLabel getStatusComponent() {
		return statusComponent;
	}
	
	public MediaComponent getMediaComponent() {
		return mediaComponent;
	}
	
	public DisplayMedia(Media media) {
		this.media = media;
		this.frame = new JFrame();
	}
	
	public void display() throws IOException {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		Dimension frameDimension = new Dimension(width, height);
		
		frame.setLocation(0, 0);
		frame.setPreferredSize(frameDimension);
		frame.setMaximumSize(frameDimension);
		frame.setMinimumSize(frameDimension);
		frame.setTitle("Browsing Media");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container pane = frame.getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBackground(Color.GRAY);

		int statusHeight = 50;
		Dimension menuDimension = new Dimension(width, statusHeight);
		LayoutManager flow = new FlowLayout();
		menuComponent = new JPanel(flow);
		menuComponent.setMaximumSize(menuDimension);
		menuComponent.setMaximumSize(menuDimension);
		menuComponent.setPreferredSize(menuDimension);
		backButton = new Button("Back");
		backButton.setLocation(new Point(10,10));
		menuComponent.add(backButton);
		statusComponent = new JLabel("Display the status here");
		menuComponent.add(statusComponent);
		menuComponent.setBackground(Color.GRAY);
		pane.add(menuComponent);

		int contentHeight = height - statusHeight;
		Dimension contentDimension = new Dimension(width, contentHeight);
		contentComponent = new JPanel();
		LayoutManager overlay = new OverlayLayout(contentComponent);
		contentComponent.setLayout(overlay);
		contentComponent.setMaximumSize(contentDimension);
		contentComponent.setMaximumSize(contentDimension);
		contentComponent.setPreferredSize(contentDimension);
		mediaComponent = new MediaComponent(media, this);
		contentComponent.add(mediaComponent);
		contentComponent.setBackground(Color.GRAY);
		pane.add(contentComponent);

		backButton.addMouseListener(this);
		frame.pack();
		frame.setVisible(true);
		mediaComponent.displayMedia(media);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(mediaComponent != null) {
			Media m = mediaComponent.getPreviousMedia();
			if(m != null) {
				try {
					mediaComponent.displayMedia(m);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
