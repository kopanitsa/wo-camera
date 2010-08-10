package com.kopanitsa.laughingman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public final class FaceDrawerView extends View{
    private static final String TAG = "FaceDrawerView";

    private Drawable mMask;
    private Bitmap mSource;
    private FaceCatcher mFace;

    public FaceDrawerView(Context context) {
        super(context);
        init(context);
    }

    public FaceDrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public FaceDrawerView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context){
        setFocusable(true);
        mMask = context.getResources().getDrawable(R.drawable.laughingman); 
    }

    public void setResource(int resourceId){
        BitmapFactory.Options bfo = new BitmapFactory.Options(); 
        bfo.inPreferredConfig = Bitmap.Config.RGB_565; 
        mSource = BitmapFactory.decodeResource( getResources() ,resourceId, bfo);
    }

    public void setResource(Bitmap resource){
        mSource = resource;
    }
    
    public Drawable getMaskImage(){
        return mMask;
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
        // laughing man
        if(mSource != null){
            float xRatio = (float)getWidth() / mSource.getWidth(); 
            float yRatio = (float)getHeight() / mSource.getHeight();
            FaceCatcher.drawImageToCanvas(canvas, mMask, mFace, xRatio, yRatio);
            mSource = null;
        }
    }
}
