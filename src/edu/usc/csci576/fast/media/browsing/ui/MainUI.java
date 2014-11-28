package edu.usc.csci576.fast.media.browsing.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
		return collageFolderName.toAbsolutePath();
	}

	public static MainUI getInstance() {
		if (ui == null) {
			ui = new MainUI();
		}
		return ui;
	}

	public static void main(String[] args) {
		try {
			MainUI ui = MainUI.getInstance();
			ui.createCollageIfNotExists();
			List<Media> mediaList = new ArrayList<Media>();
			for (int i = 1; i <= 9; i++) {
				String fileName = String.format("./dataset/image00%d.rgb", i);
				Path filePath = Paths.get(fileName);
				Media m = new Media(filePath.toAbsolutePath(), MediaType.Image);
				mediaList.add(m);
			}
			for (int i = 10; i <= 60; i++) {
				String fileName = String.format("./dataset/image0%d.rgb", i);
				Path filePath = Paths.get(fileName);
				Media m = new Media(filePath.toAbsolutePath(), MediaType.Image);
				mediaList.add(m);
			}
			Collage collage = new Collage(mediaList, 100, 60);
			Media display = new Media(collage.getCollagedImageFileName(),
					MediaType.Collage);
			DisplayMedia image = new DisplayMedia(display);
			image.display();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getWidth(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return 352;
		} else {
			return Collage.COLLAGED_IMAGE_WIDTH;
		}
	}

	public static int getHeight(MediaType mediaType) {
		if (MediaType.Image == mediaType || MediaType.Video == mediaType) {
			return 288;
		} else {
			return Collage.COLLAGED_IMAGE_HEIGHT;
		}
	}
	
	private void createCollageIfNotExists() throws IOException {
		collageFolderName = Paths.get("dataset/collage");
		if (!Files.exists(collageFolderName.toAbsolutePath())) {
			Files.createDirectory(collageFolderName.toAbsolutePath());
		}
	}
}
