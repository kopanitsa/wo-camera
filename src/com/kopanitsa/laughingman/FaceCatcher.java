package com.kopanitsa.laughingman;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.os.Build;
import android.util.Log;

public class FaceCatcher { 
    private static final String TAG = "FaceCatcher";
    private static final int LARGE_IMAGE_WIDTH_N1 = 400;
    private static final int LARGE_IMAGE_WIDTH_XPERIA = 820;
    
    private static int LARGE_IMAGE_WIDTH = LARGE_IMAGE_WIDTH_XPERIA;
    static {
        if (Build.MANUFACTURER.indexOf("HTC")!=-1){
            LARGE_IMAGE_WIDTH = LARGE_IMAGE_WIDTH_N1;
        }
    }
    
    private static final int NUM_FACES = 3; 
    private FaceDetector mFaceDetector; 
    private FaceDetector.Face mFaces[] = new FaceDetector.Face[NUM_FACES]; 

    private PointF mEyesMidPts[] = new PointF[NUM_FACES]; 
    private float  mEyesDistance[] = new float[NUM_FACES];       

    public FaceCatcher(Bitmap originalBmp) {
        float ratio = 1.f;
        Bitmap sourceBmp = null;
        if (originalBmp.getWidth() <= LARGE_IMAGE_WIDTH) {
            sourceBmp = originalBmp;
        } else {
            ratio = originalBmp.getWidth() / LARGE_IMAGE_WIDTH;
            int w = (int)(originalBmp.getWidth()  / ratio);
            int h = (int)(originalBmp.getHeight() / ratio);
            sourceBmp = resizeBitmap(originalBmp, w, h);
        }
        
        int picWidth = sourceBmp.getWidth(); 
        int picHeight = sourceBmp.getHeight(); 

        mFaceDetector = new FaceDetector( picWidth, picHeight, NUM_FACES ); 
        int numFace = mFaceDetector.findFaces(sourceBmp, mFaces); 
        
        for (int i = 0; i < numFace; i++) { 
            FaceDetector.Face face = mFaces[i]; 
            try { 
                PointF eyesMP = new PointF(); 
                face.getMidPoint(eyesMP); 
                face.pose(FaceDetector.Face.EULER_X & FaceDetector.Face.EULER_Y & FaceDetector.Face.EULER_Z );
                mEyesDistance[i] = face.eyesDistance()*ratio; 
                mEyesMidPts[i] = eyesMP;
                mEyesMidPts[i].x *= ratio;
                mEyesMidPts[i].y *= ratio;
            } catch (Exception e) { 
            } 
        }

        if (ratio>0){
            sourceBmp.recycle();
        }
    }

    public static Bitmap resizeBitmap(Bitmap bmp,int w,int h) {
        Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(result);
        BitmapDrawable drawable=new BitmapDrawable(bmp);
        drawable.setBounds(0,0,w,h);
        drawable.draw(canvas);
        return result;
    } 

    public float[] getEyeDistance(){
        return mEyesDistance;
    }
    public PointF[] getEyePoint(){
        return mEyesMidPts;
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
            } else {
                break;
            }
        } 
    }
    
} 
