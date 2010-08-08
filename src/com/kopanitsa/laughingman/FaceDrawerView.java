package com.kopanitsa.laughingman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public final class FaceDrawerView extends View{
	private static final int NUM_FACES = 3;
	private static final String TAG = "FaceDrawerView";
    
    private PointF eyesMidPts[] = new PointF[NUM_FACES]; 
    private float  eyesDistance[] = new float[NUM_FACES]; 

    private Drawable mImage;
	private Bitmap mSource;
	private FaceCatcher mFace;
     
    private Paint samplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int picWidth, picHeight, xPosition, yPosition, xLength, yLength, xSize, ySize; 
    private float xRatio, yRatio; 
	
	public FaceDrawerView(Context context) {
		super(context);
		setFocusable(true);
		mImage = context.getResources().getDrawable(R.drawable.laughingman); 
	}

	public void setResource(int resourceId){
		BitmapFactory.Options bfo = new BitmapFactory.Options(); 
        bfo.inPreferredConfig = Bitmap.Config.RGB_565; 
		mSource = BitmapFactory.decodeResource( getResources() ,resourceId, bfo);
	}

	public void setResource(Bitmap resource){
		mSource = resource;
	}

	public void startFaceCatch(){
        try {
            if(mSource != null){
            	mFace = new FaceCatcher(mSource);
            }
        } catch (Throwable e) {
        }
	}

	@Override
	protected void onDraw(Canvas canvas){     
    	if(mSource != null){
    	
    	  samplePaint.setStyle(Paint.Style.FILL); 
          samplePaint.setTextAlign(Paint.Align.CENTER);  
          
          picWidth = mSource.getWidth(); 
          picHeight = mSource.getHeight(); 
          eyesDistance = mFace.getEyeDistance();
          eyesMidPts = mFace.getEyePoint();

          
          xRatio = getWidth()*1.0f / picWidth; 
          yRatio = getHeight()*1.0f / picHeight;
          
          for (int i = 0; i < eyesMidPts.length; i++) 
          {               
              if (eyesMidPts[i] != null) 
              {
                  xSize = (int) (eyesDistance[i]*2);
                  ySize =  (int) (eyesDistance[i]*2);

                  xPosition = (int)(eyesMidPts[i].x*xRatio)-xSize;
                  yPosition = (int)(eyesMidPts[i].y*yRatio)-ySize;

                  xLength = (int)(eyesMidPts[i].x*xRatio)+xSize;
                  yLength = (int)(eyesMidPts[i].y*yRatio)+ySize;		

                  mImage.setBounds(xPosition, yPosition, xLength, yLength);
                  mImage.setVisible(true, true);
                  mImage.draw(canvas);
              } 
              mSource = null;
          } 
    	}
     }
}
