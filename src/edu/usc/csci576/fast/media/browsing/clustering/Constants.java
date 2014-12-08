package edu.usc.csci576.fast.media.browsing.clustering;

public class Constants {
	
	public static final int IMAGE_HEIGHT = 288;
	public static final int IMAGE_WIDTH = 352;
	public static final int COLLAGED_IMAGE_WIDTH = 600;
	public static final int COLLAGED_IMAGE_HEIGHT = 600;
	public static final int NUMBER_OF_FRAMES = 300;
	public static final int IMAGE_LENGTH = IMAGE_WIDTH * IMAGE_HEIGHT * 3;
	public static final int COLLAGE_LENGTH = COLLAGED_IMAGE_WIDTH * COLLAGED_IMAGE_HEIGHT * 3;
	public static final int VIDEO_LENGTH = IMAGE_LENGTH * NUMBER_OF_FRAMES;
	/*public static final double BATTACHARYA_THRESHOLD = 0;
	public static final double CHISQR_THRESHOLD = 0;
	public static final double CORREL_THRESHOLD = 0;
	public static final double HELLINGER_THRESHOLD = 0;*/
	public static final double INTERSECTION_THRESHOLD = 50000;
	public static final int COLLAGE_THRESHOLD = 60;
	
	public static int CARTOON_THRESHOLD = -4000;

}
