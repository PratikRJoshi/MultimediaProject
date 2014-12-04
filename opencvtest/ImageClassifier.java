package opencvtest;
import java.awt.image.BufferedImage;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

import static java.nio.file.StandardCopyOption.*;
//import opencv2/nonfree/nonfree.hpp"
public class ImageClassifier {
	static double C[] = new double[8];
	static double COS[][] = new double[8][8];
	
	public static void convertRGBtoJPG(){
		
		imageReader image = new imageReader();
		String imageArr[] = new String[2];
		
		File folder = new File("C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1");
		int i=1;
		for (File fileEntry : folder.listFiles()) {
	        System.out.println(i++ +" "+fileEntry.getName());
	        String fileName = fileEntry.getName().split("\\.")[0];
			imageArr[0]="C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1\\"+fileEntry.getName();
			imageArr[1]=(fileName)+".jpg";
			System.out.println(imageArr[1]);
			
			image.main(imageArr);
	        
	    }

	}
	
	public static int getPeakValueForImage(Mat hueMat){
		
		byte data[]= new byte[(int) (hueMat.total()*hueMat.channels())];
		byte countMat[]=new byte[360];
		//System.out.println("rows "+hueMat.rows()+"columns "+hueMat.cols()+" totals "+hueMat.total());
		//hueMat.get(0, 0, data);
		
		for(int i=0;i<288;i++){
			for(int j=0;j<352;j++)
			countMat[(int)hueMat.get(i, j)[0]]++;
		}
		
		int peakValue = countMat[0];
		int peakIndex= 0;
		for(int i=0;i<countMat.length;i++){
			if(peakValue < countMat[i]){
				peakValue = countMat[i];
				peakIndex=i;
			}
		}
		System.out.println("The peak index for this image is "+peakIndex+" value is "+peakValue);
		return peakIndex;
	}

	public static HashMap<String, Integer> getImages(String dir){
		
		File imgDir=new File(dir);
		HashMap<String , Integer> mapping = new HashMap<String , Integer>();
		List<PeakObject> mappingList = new ArrayList<PeakObject>();
		int index = 0;
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat hsvimg = new Mat();
			Imgproc.cvtColor(image, hsvimg, Imgproc.COLOR_BGR2HSV);
			int peakValue=getPeakValueForImage(hsvimg);
			mapping.put(fileEntry.getAbsolutePath(), peakValue);
			 
		}
		System.out.println("Size of map "+mapping.size());
		return mapping;
		
	}
	public static void createBuckets(HashMap<String, Double> peakMap,double lowerLimit,double upperLimit){
		Iterator it = peakMap.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        if((double)pairs.getValue()>lowerLimit && (double)pairs.getValue()<=upperLimit){
	        	System.out.println("Image path "+ pairs.getKey()+" "+"peakvalue "+pairs.getValue());
	        	File src= new File((String)pairs.getKey());
	        	File dest=new File("C:\\outputCartoon\\"+src.getName());
	        	
	        	try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
		}
	}
	
	public static void createBuckets(HashMap<String, Integer> peakMap,int lowerLimit,int upperLimit){
		Iterator it = peakMap.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        if((int)pairs.getValue()>lowerLimit && (int)pairs.getValue()<=upperLimit){
	        	System.out.println("Image path "+ pairs.getKey()+" "+"peakvalue "+pairs.getValue());
	        	File src= new File((String)pairs.getKey());
	        	File dest=new File("C:\\output\\"+src.getName());
	        	
	        	try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
		}
	}
	
	public static void separateCartoonImages(HashMap<String, Double> filterMap){
		Iterator it = filterMap.entrySet().iterator();
		int i =1;
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        if((double)pairs.getValue()<-0.01){
	        	System.out.println("Image path "+ pairs.getKey()+" "+"peakvalue "+pairs.getValue());
	        	File src= new File((String)pairs.getKey());
	        	File dest=new File("C:\\outputCartoon\\"+src.getName());
	        	
	        	try {
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        System.out.println("Cartoon image found: "+(i++));
		}
		
	}
	
	private static void copyFileUsingFileChannels(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}
	
	public static void setCOSValues(){
//		double cos[][] = cosArray;
		//System.out.println(Math.acos(-1));
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				COS[i][j]  = Math.cos(((2 * j + 1) * i * Math.acos(-1))/16.0);			//i and j should interchange their positions
			
			if(i == 0 || j==0)
				C[i] = 1/Math.sqrt(2);
			else
				C[i] = 1;
			}
		}
		
	}
	
	public static HashMap<String,Double> classifyByFilter(String dir){
		File imgDir=new File(dir);
		HashMap<String , Double> mapping = new HashMap<String , Double>();
		
		for (File fileEntry : imgDir.listFiles()) {
			Mat src=Highgui.imread(fileEntry.getAbsolutePath());
			Mat dst=new Mat();
			Mat dstHSV=new Mat();
			Mat srcHSV=new Mat();
			Imgproc.bilateralFilter(src, dst, 5, 250, 250);

			double meanValue=0;

			//Imgproc.cvtColor(src, srcHSV, Imgproc.COLOR_RGB2YUV);
			//Imgproc.cvtColor(dst, dstHSV, Imgproc.COLOR_RGB2YUV);

			for(int i=0;i<288;i++)
				for(int j=0;j<352;j++)
					meanValue+=src.get(i,j)[0]-dst.get(i,j)[0];

			meanValue/=352*288;
			mapping.put(fileEntry.getAbsolutePath(), meanValue);
		}
		return mapping;
			//System.out.println("Mean is "+meanValue);

			//Highgui.imwrite("c:\\testcartoon.jpg", dst);
		}
	public static HashMap<String, Double> getDCTValues(String dir){
		File imgDir=new File(dir);
		HashMap<String , Double> mapping = new HashMap<String , Double>();
		setCOSValues();
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2YUV);
			//System.out.println("here" +yuvimg.get(0, 0)[0]);
			double dctBuffer[][] = PerformDCT.applyDCT(288, 352, yuvimg,COS,C);
			//printMat(dctBuffer);
			double freqValue=PerformDCT.readZigZag(288, 352, dctBuffer);
			System.out.println("Frequency value for image :"+fileEntry.getName()+" is "+freqValue);
			mapping.put(fileEntry.getAbsolutePath(), freqValue);
			//System.exit(0);
			
		}
		System.out.println("Size of map "+mapping.size());
		return mapping;
		
		
	}
	
	public static Map<Integer, List<String>> sortByKMeans(String dir){
		File imgDir=new File(dir);
		int numberOfBaseImages = 10;
		double meanArr[]=new double[numberOfBaseImages];

		int randImages[]=new int[numberOfBaseImages];
		Random number=new Random();
		int  randImg=0;
		Map<Integer,List<String>> collageList=new HashMap<Integer,List<String>>();
		
		for(int i=0;i<numberOfBaseImages;i++)
		{
			randImg= number.nextInt(300);
			if(randImg!=0)
				randImages[i]=randImg;
		}
		Map<String,	Integer> imageClusterMap=new HashMap<String,Integer>();
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2YUV);
			
						
			for(int i = 0; i < numberOfBaseImages; i++){
				
				double tempVal=0;
				
				Mat randRef=Highgui.imread("C:\\dataset1\\outputImage_"+randImages[i]+".jpg");
				
				Mat yuvRandRef=new Mat();
				Imgproc.cvtColor(randRef, yuvRandRef, Imgproc.COLOR_RGB2YUV);
				
				
				for(int j = 0; j < 288; j++){
					for(int k = 0; k < 352; k++){
						tempVal+=Math.pow((yuvRandRef.get(j, k)[0]-yuvimg.get(j, k)[0]),2);
					}
				}
				meanArr[i]=Math.sqrt(tempVal);
				
			}
			int index=findMin(meanArr);
			System.out.println("Inserting into index "+index);
			//imageClusterMap.put(fileEntry.getAbsolutePath(), index);
			//List<String> imgList = new ArrayList<String>();
			if(collageList.get(index)==null){
				List<String> tempList= new ArrayList<String>();
				tempList.add(fileEntry.getName());
				collageList.put(index,tempList);
			}
			else{
				collageList.get(index).add(fileEntry.getName());
			}
			
//			File file = new File("C:\\kmeans\\"+index);
//			File src=new File(fileEntry.getAbsolutePath());
//        	if (!file.exists()) {
//        		if (file.mkdir()) {
//        			File dest=new File("C:\\kmeans\\"+index+"\\"+src.getName());
//        			try {
//        				System.out.println("Creating bucketno "+index);
//    					copyFileUsingFileChannels(src, dest);
//    				} catch (IOException e) {
//    					e.printStackTrace();
//    				}
//        		} else {
//        			System.out.println("Failed to create directory!");
//        		}
//        	}
//        	else{
//        		File dest=new File("C:\\kmeans\\"+index+"\\"+src.getName());
//    			try {
//    				System.out.println("Inserting in bucketno "+index);
//					copyFileUsingFileChannels(src, dest);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//        	}

		}
		return collageList;
			
	}

	public static void sortByKMeans1(String dir){
		File imgDir=new File(dir);
		Mat ref1=Highgui.imread("C:\\dataset1\\outputImage_1.jpg");
		Mat ref2=Highgui.imread("C:\\dataset1\\outputImage_23.jpg");
		Mat ref3=Highgui.imread("C:\\dataset1\\outputImage_123.jpg");
		Mat ref4=Highgui.imread("C:\\dataset1\\outputImage_199.jpg");
		Mat ref5=Highgui.imread("C:\\dataset1\\outputImage_248.jpg");
		
//		Mat ref1=Highgui.imread("C:\\kmeans\\0\\outputImage_67.jpg");
//		Mat ref2=Highgui.imread("C:\\kmeans\\0\\outputImage_98.jpg");
//		Mat ref3=Highgui.imread("C:\\kmeans\\0\\outputImage_123.jpg");
//		Mat ref4=Highgui.imread("C:\\kmeans\\0\\outputImage_151.jpg");
//		Mat ref5=Highgui.imread("C:\\kmeans\\0\\outputImage_232.jpg");
		
		Mat yuvref1=new Mat();
		Mat yuvref2=new Mat();
		Mat yuvref3=new Mat();
		Mat yuvref4=new Mat();
		Mat yuvref5=new Mat();
		
		
		Imgproc.cvtColor(ref1, yuvref1, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref2, yuvref2, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref3, yuvref3, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref4, yuvref4, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(ref5, yuvref5, Imgproc.COLOR_RGB2HSV);
		
		int pk1=getPeakValueForImage(yuvref1);
		int pk2=getPeakValueForImage(yuvref2);
		int pk3=getPeakValueForImage(yuvref3);
		int pk4=getPeakValueForImage(yuvref4);
		int pk5=getPeakValueForImage(yuvref5);
		
		int meanArr[]=new int[5];
		
		for (File fileEntry : imgDir.listFiles()) {
			Mat image = Highgui.imread(fileEntry.getAbsolutePath());
			Mat yuvimg = new Mat();
			Imgproc.cvtColor(image, yuvimg, Imgproc.COLOR_RGB2HSV);
			
			int pkimg=getPeakValueForImage(yuvimg);
			
			meanArr[0]=Math.abs(pk1-pkimg);
			meanArr[1]=Math.abs(pk2-pkimg);
			meanArr[2]=Math.abs(pk3-pkimg);
			meanArr[3]=Math.abs(pk4-pkimg);
			meanArr[4]=Math.abs(pk5-pkimg);
			System.out.println(pk1+" "+pk2+" "+pk3+" "+pk4+" "+pk5+" "+pkimg+" ");
			int index=findMin(meanArr);
			
			File file = new File("C:\\kmeans\\"+index);
			File src=new File(fileEntry.getAbsolutePath());
        	if (!file.exists()) {
        		if (file.mkdir()) {
        			File dest=new File("C:\\kmeans\\"+index+"\\"+src.getName());
        			try {
        				System.out.println("Creating bucketno "+index);
    					copyFileUsingFileChannels(src, dest);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
        		} else {
        			System.out.println("Failed to create directory!");
        		}
        	}
        	else{
        		File dest=new File("C:\\kmeans\\"+index+"\\"+src.getName());
    			try {
    				System.out.println("Inserting in bucketno "+index);
					copyFileUsingFileChannels(src, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}

		}
			
	}

	public static int findMin(double array[]){
		 double min = Double.MAX_VALUE;
		 int minIndex=0;
	        for (int i = 0; i < array.length; i++) {
	            if (array[i] < min) {
	                min = array[i];
	                minIndex=i;
	            }
	        }
	        return minIndex;
		
	}
	
	public static int findMin(int array[]){
		 int min = Integer.MAX_VALUE;
		 int minIndex=0;
	        for (int i = 0; i < array.length; i++) {
	            if (array[i] < min) {
	                min = array[i];
	                minIndex=i;
	            }
	        }
	        return minIndex;
		
	}
	
	public static void printMat(double input[][]){
		for(int i = 0; i < 50; i++){
			for(int j = 0; j < 50; j++){
				System.out.print(input[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static void separateFaceImages(List<String> faceImages){
		
		for(int i=0 ; i <faceImages.size();i++){
			File src= new File(faceImages.get(i));
        	File dest=new File("C:\\outputFace\\"+src.getName());
        	
        	try {
				copyFileUsingFileChannels(src, dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	System.out.println("Iteration "+(i+1)+" finished");
		}
	}
	
	public static void createBuckets(HashMap<String, Integer> imageMap,int bucketSize){
		Iterator it = imageMap.entrySet().iterator();
		new File("c:\\buckets").mkdir();
		
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //if((int)pairs.getValue()>hueValue && (int)pairs.getValue()<=(hueValue+bucketSize)){
	        	System.out.println("Image path "+ pairs.getKey()+" "+"peakvalue "+pairs.getValue());
	        	File src= new File((String)pairs.getKey());
	        	int bucketNo=(int)pairs.getValue()/bucketSize; 
	        	//String bucketNo="bucket"+index;
	        	File file = new File("C:\\buckets\\"+bucketNo);
	        	if (!file.exists()) {
	        		if (file.mkdir()) {
	        			File dest=new File("C:\\buckets\\"+bucketNo+"\\"+src.getName());
	        			try {
	        				System.out.println("Creating bucketno "+bucketNo);
	    					copyFileUsingFileChannels(src, dest);
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    				}
	        		} else {
	        			System.out.println("Failed to create directory!");
	        		}
	        	}
	        	else{
	        		File dest=new File("C:\\buckets\\"+bucketNo+"\\"+src.getName());
        			try {
        				System.out.println("Inserting in bucketno "+bucketNo);
    					copyFileUsingFileChannels(src, dest);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
	        	}
	        	
		}
		
	}
	
	
	public static void main( String[] args ) throws IOException
	   {
		
		 System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		 //convertRGBtoJPG();
		 Map<Integer, List<String>> collageMap=sortByKMeans(args[0]);
//		 HashMap<String,Integer> peakMap=getImages(args[0]);
//		 
//		 int lowerLimit=0;
//		 int upperLimit=50;
//		 //createBuckets(peakMap,lowerLimit,upperLimit);
//		 createBuckets(peakMap, 30);
//	 
		 Iterator it = collageMap.entrySet().iterator();
		 List<Media> listOfCollage = new ArrayList<Media>();
		 MainUI ui = MainUI.getInstance();
		 ui.createCollageIfNotExists();
	
		 while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		    
			List<Media> listOfMedia = new ArrayList<Media>();
		 //give the directory to the dataset		 
			List<String> images = (ArrayList<String>)pairs.getValue();
			for (int i=0;i<images.size();i++) {
			 System.out.println(images.get(i));
			 Path p = Paths.get("C:\\Users\\Neeraj\\Desktop\\CS576_Project_Fall_2014\\CS576_Project_Dataset_1\\"+images.get(i).split("\\.")[0]+".rgb");
			 Media m = new Media(p, MediaType.Image);
			 listOfMedia.add(m);
		 }
//		 
//		 //System.out.println(listOfMedia.get(0).getFilePath().toString());
//		 //System.exit(0);
		 
		 Collage collage = new Collage(listOfMedia, 50, 50);
		 Media coll=new Media(collage.getCollagedImageFileName(), MediaType.Collage);
		 listOfCollage.add(coll);
		 //Path cpath=Paths.get(args[1]+"\\"+collage);
		 }
		 Collage newCollage = new Collage(listOfCollage, 50, 50);
		 Media display = new Media(newCollage.getCollagedImageFileName(), MediaType.Collage);
		 DisplayMedia image = new DisplayMedia(display);
		 image.display();
		 
//		 
//		 HashMap<String,Double> filterMap = classifyByFilter(args[1]);
//		 separateCartoonImages(filterMap);
// 
//		 FaceDetector detect=new FaceDetector();
//		 List<String> faceImages=detect.faceDetect(args[0]);
//		 separateFaceImages(faceImages);
		 		 
		 //HashMap<String,Double> dctMap = getDCTValues(args[0]);  
		 //createBuckets(dctMap,1566.0,1650.0);
		 	    	     
//	     //convertRGBtoJPG();
//		 
//		 Mat img_1 = Highgui.imread(args[0]);
//		 Mat img_2 = Highgui.imread(args[1]);
//		 
//		 //Highgui.im
//		 Mat hsvimg1 = new Mat();
//		 Mat hsvimg2 = new Mat();
//		 Mat img_out = new Mat();
//		 
//		 int minHessian = 400;
//		 
//		 
//		 //Imgproc.cvt
////		 FeatureDetector surf = FeatureDetector.create(FeatureDetector.SURF);
////		 
////		 MatOfKeyPoint kp3=new MatOfKeyPoint();
////		 
////		 surf.detect( img_1, kp3 );
////		 Features2d.drawKeypoints(img_1, kp3, img_out);
////		 
////		 Highgui.imwrite("c:\\output2.png", img_out);
////		 
//		 //int code= 
//		 System.out.println("Number of channels 2 "+img_2.channels());
//		 System.out.println("Number of channels "+img_1.channels());
//		 
//		 
//		 //Imgproc.cvtColor(img_1, img_1, Imgproc.COLOR_GRAY2BGR);
//		 //Imgproc.cvtColor(img_2, img_2, Imgproc.COLOR_GRAY2BGR);
//		 
//		 //System.out.println("Number of channels "+img_1.channels());
//		 
//		 
//		 Imgproc.cvtColor(img_1, hsvimg1, Imgproc.COLOR_BGR2HSV);
//		 Imgproc.cvtColor(img_2, hsvimg2, Imgproc.COLOR_BGR2HSV);
//		 
//		 getPeakValueForImage(hsvimg1);
//		 
//			 
//		 /// Using 50 bins for hue and 60 for saturation
//		 int h_bins = 50; int s_bins = 60;
//		 int histSize1[] = { h_bins, s_bins };
//		 MatOfInt hbins=new MatOfInt(50);
//		 MatOfInt sbins=new MatOfInt(60);
//		 
//		 MatOfInt histSize = new MatOfInt(50,60);
//		    // hue varies from 0 to 179, saturation from 0 to 255
//		  //float h_ranges[] = { 0, 180 };
//		 MatOfFloat ranges= new MatOfFloat(0f,180f,0f,256f);
//		  
//		  //float ranges[][] = { h_ranges, s_ranges };
//
//		    // Use the o-th and 1-st channels
//		   //int channels1[] = { 0, 1, 2 };
//		   MatOfInt channels = new MatOfInt(0,1);
//		   
//		   List<Mat> list1 = new ArrayList<>();
//		   list1.add(hsvimg1);
//		   
//		   List<Mat> list2 = new ArrayList<>();
//		   list2.add(hsvimg2);
//		   
//		   /// Histograms
//		   
//		    Mat hist_test1 = new Mat();
//		    Mat hist_test2 = new Mat();
//
//		    /// Calculate the histograms for the HSV images
//		    Imgproc.calcHist(list1, channels, new Mat(), hist_test1, histSize, ranges);
//		    Imgproc.calcHist(list2, channels, new Mat(), hist_test2, histSize, ranges);
//		    
//		    //System.out.println("Output"+img_1.+" "+hist_test1.dims());
//		    //System.out.println("Output 2"+img_2.dims()+" "+hist_test2.dims());
//		    if(hist_test1.equals(hist_test2))
//		    	System.out.println("Are equal");
//		    
//		    Core.normalize(hist_test1, hist_test1, 0, 1, 32,-1);
//		    Core.normalize(hist_test2, hist_test2, 0, 1, 32,-1);
//		    
//		    //Core.norm
//		    double result1=Imgproc.compareHist(hist_test1, hist_test1, 0);
//		    double result2=Imgproc.compareHist(hist_test1, hist_test2, 0);
//		    
//		    System.out.println("The result is "+result1+" "+ result2);
//		    //calcHist( hsvimg1, 1, channels, Mat(), hist_base, 2, histSize, ranges, true, false );
//		    //normalize( hist_base, hist_base, 0, 1, NORM_MINMAX, -1, Mat() );
//
//		    //calcHist( hsvimg2, 1, channels, Mat(), hist_test2, 2, histSize, ranges, true, false );
//		    //normalize( hist_test2, hist_test2, 0, 1, NORM_MINMAX, -1, Mat() );
//		 
//		 //outputImage.setIcon(new javax.swing.ImageIcon("c:\\Output.png"));
//		 //outputImage.setIcon(new ImageIcon(ImageIO.read(new File("c:\\Output.png")))); 
		 
		 
		 
	   }
}
