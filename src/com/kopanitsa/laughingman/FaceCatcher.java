package com.kopanitsa.laughingman;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;

public class FaceCatcher { 
     private static final int NUM_FACES = 7; 
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
     
     public static final void drawImageToCanvas(Canvas canvas, Drawable mask, 
             FaceCatcher face){
         drawImageToCanvas(canvas, mask, face, 1.f, 1.f);
     }
     
     public static final void drawImageToCanvas(Canvas canvas, Drawable mask, 
             FaceCatcher face, float xRatio, float yRatio){
         PointF eyesMidPts[] = new PointF[NUM_FACES]; 
         float  eyesDistance[] = new float[NUM_FACES]; 

         eyesDistance = face.getEyeDistance();
         eyesMidPts = face.getEyePoint();

         for (int i = 0; i < eyesMidPts.length; i++) {
             if (eyesMidPts[i] != null) {
                 int xPosition, yPosition, xLength, yLength, xSize, ySize; 
                 
                 xSize = (int) (eyesDistance[i]*2);
                 ySize =  xSize;

                 xPosition = (int)(eyesMidPts[i].x*xRatio)-xSize;
                 yPosition = (int)(eyesMidPts[i].y*yRatio)-ySize;

                 xLength = (int)(eyesMidPts[i].x*xRatio)+xSize;
                 yLength = (int)(eyesMidPts[i].y*yRatio)+ySize;      

                 mask.setBounds(xPosition, yPosition, xLength, yLength);
                 mask.setVisible(true, true);
                 mask.draw(canvas);
             } 
         } 
     }
} 
