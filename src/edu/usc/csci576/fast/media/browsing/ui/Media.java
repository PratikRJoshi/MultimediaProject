package edu.usc.csci576.fast.media.browsing.ui;

import java.nio.file.Path;

/**
 * @author Sathish Srinivasan
 */
public class Media {

	private Path filePath;
	private MediaType fileType;

	public Media(Path filePath, MediaType fileType ) {
		this.filePath = filePath;
		this.fileType = fileType;
	}
	
	public Path getFilePath() {
		return filePath;
	}
	
	public MediaType getFileType() {
		return fileType;
	}
}
