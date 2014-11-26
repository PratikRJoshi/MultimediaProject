package edu.usc.csci576.fast.media.browsing.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

/**
 * @author Sathish Srinivasan
 */
public class DisplayMedia {
	private Media media;
	private JFrame frame;

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

		int statusHeight = 20 * height / 100;
		Dimension statusDimension = new Dimension(width, statusHeight);
		JLabel statusComponent = new JLabel("Display the status here");
		statusComponent.setMaximumSize(statusDimension);
		statusComponent.setMaximumSize(statusDimension);
		statusComponent.setPreferredSize(statusDimension);
		pane.add(statusComponent);

		int contentHeight = height - statusHeight;
		Dimension contentDimension = new Dimension(width, contentHeight);
		JPanel contentComponent = new JPanel();
		LayoutManager overlay = new OverlayLayout(contentComponent);
		contentComponent.setLayout(overlay);
		contentComponent.setMaximumSize(contentDimension);
		contentComponent.setMaximumSize(contentDimension);
		contentComponent.setPreferredSize(contentDimension);
		MediaComponent mediaComponent = new MediaComponent(media, statusComponent);
		contentComponent.add(mediaComponent);
		contentComponent.setBackground(Color.GRAY);
		pane.add(contentComponent);

		frame.pack();
		frame.setVisible(true);
		mediaComponent.displayMedia(frame);
	}

}
