package edu.usc.csci576.fast.media.browsing.clustering;

import javax.swing.*;

import org.opencv.core.Mat;

public class PerformDCT{
	static double C[] = new double[8];
	static double COS[][] = new double[8][8];
	static double redBuffer[][], greenBuffer[][], blueBuffer[][];
	
	static double dctRedBuffer[][], dctGreenBuffer[][], dctBlueBuffer[][];
	static double idctRedBuffer[][], idctGreenBuffer[][], idctBlueBuffer[][];
	static byte[] bytes;
	static JFrame frame=null;
	static JLabel label;
	

	
	public static void setCOSValues(double cosArray[][]){
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
	
	public static double readZigZag(int height, int width, double buffer[][]){
		
		int size=8;
		double interiorSum = 0.0;
		
		for(int row = 0; row < (height/8); row++){
			for(int col = 0; col < (width/8); col++){
					int tempi=1;
						int tempj=1;
						int count=0;
						for(int u = 0; u < 8; u++){
							for(int v = 0; v < 8; v++){
									if(count<=8){
										
										interiorSum+= buffer[row * 8 + tempi-1][col * 8 + tempj-1];
										//System.out.println("Value taken "+interiorSum);
									}
						        	
								
								count++;
					            
					                if ((tempi + tempj) % 2 == 0){
					                   if (tempj < size)
					                        tempj++;
					                    else
					                        tempi+= 2;
					                    if (tempi > 1)
					                        tempi--;
					                }
					                else{
					                   if (tempi < size)
					                        tempi++;
					                    else
					                        tempj+= 2;
					                    if (tempj > 1)
					                        tempj--;
					                }
							}
							
						}
						//System.exit(0);
					}
				
			
		}
		
		return interiorSum;
	}
	
	public static double[][] applyDCT(int height, int width, Mat yuvimg){
		double dctBuffer[][] = new double[height][width];
		int size=8;
			setCOSValues(COS);
			for(int row = 0; row < (height/8); row++){
			for(int col = 0; col < (width/8); col++){
				int tempi=1;
				int tempj=1;
				int count=0;
				for(int u = 0;u < 8; u++){
					for(int v = 0; v < 8; v++){
						double tempSum = 0.0;
						for(int x = 0; x < 8; x++){
							for(int y = 0; y < 8; y++){
									tempSum += (yuvimg.get((8 * row) + x,(8 * col) + y)[0]) * COS[u][x] * COS[v][y];
								}
							
						}
						tempSum *= 0.25 * C[u] * C[v];
						dctBuffer[row*8+u][col*8+v] = tempSum;
						}
					
					 }
				}
				}
			
		return dctBuffer;
		//return resultbuffer;
	}
	
	
	
}