package com.kopanitsa.laughingman;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;

public class FaceCatcher { 
     private static final int NUM_FACES = 3; 
     private FaceDetector arrayFaces; 
     private FaceDetector.Face getAllFaces[] = new FaceDetector.Face[NUM_FACES]; 
     private FaceDetector.Face getFace = null; 
      
     private PointF eyesMidPts[] = new PointF[NUM_FACES]; 
     private float  eyesDistance[] = new float[NUM_FACES];       
     private int picWidth, picHeight;
      
     public FaceCatcher(Bitmap sourceImage) { 
    	 
    	  picWidth = sourceImage.getWidth(); 
          picHeight = sourceImage.getHeight(); 

          arrayFaces = new FaceDetector( picWidth, picHeight, NUM_FACES ); 
          arrayFaces.findFaces(sourceImage, getAllFaces); 
          for (int i = 0; i < getAllFaces.length; i++) 
          { 
               getFace = getAllFaces[i]; 
               try { 
                    PointF eyesMP = new PointF(); 
                    getFace.getMidPoint(eyesMP); 
                    getFace.pose(FaceDetector.Face.EULER_X & FaceDetector.Face.EULER_Y & FaceDetector.Face.EULER_Z );
                    eyesDistance[i] = getFace.eyesDistance(); 
                    eyesMidPts[i] = eyesMP; 
               } 
               catch (Exception e) 
               { 
               } 
           
          }
     }

     public float[] getEyeDistance(){
		return eyesDistance;
     }
     public PointF[] getEyePoint(){
 		return eyesMidPts;
      }
     
     
} 
