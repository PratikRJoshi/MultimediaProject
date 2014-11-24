package edu.usc.csci576.fast.media.browsing.ui;

import java.nio.file.Path;

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

	public Path getCollageFolderName() {
		return collageFolderName;
	}

	public static MainUI getInstance() {
		if(ui == null) {
			ui = new MainUI();
		}
		return ui;
	}

}
