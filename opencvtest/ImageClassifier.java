package opencvtest;
import java.awt.image.BufferedImage;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

//import opencv2/nonfree/nonfree.hpp"
public class ImageClassifier {
	
	public static void convertRGBtoJPG(){
		
		imageReader image = new imageReader();
		String imageArr[] = new String[2];
		
		File folder = new File("C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1");
		int i=1;
		for (File fileEntry : folder.listFiles()) {
	            //System.out.println(i++ +" "+fileEntry.getName());
			imageArr[0]="C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1\\"+fileEntry.getName();
			imageArr[1]="outputImage_"+(i++)+".jpg";
			image.main(imageArr);
	        
	    }
		
	}

	 public static void main( String[] args ) throws IOException
	   {
	     System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	    	     
	     //convertRGBtoJPG();
		 
		 Mat img_1 = Highgui.imread(args[0]);
		 Mat img_2 = Highgui.imread(args[1]);
		 
		 //Highgui.im
		 Mat hsvimg1 = new Mat();
		 Mat hsvimg2 = new Mat();
		 Mat img_out = new Mat();
		 
		 int minHessian = 400;
		 
		 
		 //Imgproc.cvt
//		 FeatureDetector surf = FeatureDetector.create(FeatureDetector.SURF);
//		 
//		 MatOfKeyPoint kp3=new MatOfKeyPoint();
//		 
//		 surf.detect( img_1, kp3 );
//		 Features2d.drawKeypoints(img_1, kp3, img_out);
//		 
//		 Highgui.imwrite("c:\\output2.png", img_out);
//		 
		 //int code= 
		 System.out.println("Number of channels 2 "+img_2.channels());
		 System.out.println("Number of channels "+img_1.channels());
		 
		 
		 //Imgproc.cvtColor(img_1, img_1, Imgproc.COLOR_GRAY2BGR);
		 //Imgproc.cvtColor(img_2, img_2, Imgproc.COLOR_GRAY2BGR);
		 
		 //System.out.println("Number of channels "+img_1.channels());
		 
		 
		 Imgproc.cvtColor(img_1, hsvimg1, Imgproc.COLOR_BGR2HSV);
		 Imgproc.cvtColor(img_2, hsvimg2, Imgproc.COLOR_BGR2HSV);
		 
		
		 
			 
		 /// Using 50 bins for hue and 60 for saturation
		 int h_bins = 50; int s_bins = 60;
		 int histSize1[] = { h_bins, s_bins };
		 MatOfInt hbins=new MatOfInt(50);
		 MatOfInt sbins=new MatOfInt(60);
		 
		 MatOfInt histSize = new MatOfInt(50,60);
		    // hue varies from 0 to 179, saturation from 0 to 255
		  //float h_ranges[] = { 0, 180 };
		 MatOfFloat ranges= new MatOfFloat(0f,180f,0f,256f);
		  
		  //float ranges[][] = { h_ranges, s_ranges };

		    // Use the o-th and 1-st channels
		   //int channels1[] = { 0, 1, 2 };
		   MatOfInt channels = new MatOfInt(0,1);
		   
		   List<Mat> list1 = new ArrayList<>();
		   list1.add(hsvimg1);
		   
		   List<Mat> list2 = new ArrayList<>();
		   list2.add(hsvimg2);
		   
		   /// Histograms
		   
		    Mat hist_test1 = new Mat();
		    Mat hist_test2 = new Mat();

		    /// Calculate the histograms for the HSV images
		    Imgproc.calcHist(list1, channels, new Mat(), hist_test1, histSize, ranges);
		    Imgproc.calcHist(list2, channels, new Mat(), hist_test2, histSize, ranges);
		    
		    //System.out.println("Output"+img_1.+" "+hist_test1.dims());
		    //System.out.println("Output 2"+img_2.dims()+" "+hist_test2.dims());
		    if(hist_test1.equals(hist_test2))
		    	System.out.println("Are equal");
		    
		    Core.normalize(hist_test1, hist_test1, 0, 1, 32,-1);
		    Core.normalize(hist_test2, hist_test2, 0, 1, 32,-1);
		    
		    //Core.norm
		    double result1=Imgproc.compareHist(hist_test1, hist_test1, 0);
		    double result2=Imgproc.compareHist(hist_test1, hist_test2, 0);
		    
		    System.out.println("The result is "+result1+" "+ result2);
		    //calcHist( hsvimg1, 1, channels, Mat(), hist_base, 2, histSize, ranges, true, false );
		    //normalize( hist_base, hist_base, 0, 1, NORM_MINMAX, -1, Mat() );

		    //calcHist( hsvimg2, 1, channels, Mat(), hist_test2, 2, histSize, ranges, true, false );
		    //normalize( hist_test2, hist_test2, 0, 1, NORM_MINMAX, -1, Mat() );
		 
		 //outputImage.setIcon(new javax.swing.ImageIcon("c:\\Output.png"));
		 //outputImage.setIcon(new ImageIcon(ImageIO.read(new File("c:\\Output.png")))); 
		 
		 
		 
	   }
}
