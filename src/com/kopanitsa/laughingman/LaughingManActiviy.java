package com.kopanitsa.laughingman;

import java.io.IOException;

import com.kopanitsa.common.camera.CameraUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

public class LaughingManActiviy extends Activity {
    private static final String TAG = "LaughingManActiviy";
    private Camera mCamera;
    private Point mResolution;
    private FaceDrawerView mFaceDrawer;
    private SurfaceView mSurface;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSurface = new SurfaceView(this);
        
        SurfaceHolder holder = mSurface.getHolder();
        holder.addCallback(mSurfaceListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS  );
        setContentView(mSurface);
        mFaceDrawer = new FaceDrawerView(this);
        this.addContentView(mFaceDrawer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    

    private SurfaceHolder.Callback mSurfaceListener = 
        new SurfaceHolder.Callback() {
    
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mCamera != null){
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
        
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if(mCamera != null){
                Camera.Parameters parameters = mCamera.getParameters();
                // for debug
                //getNicePreviewSize(parameters);
                
                parameters.setPreviewSize(w, h);
                //parameters.setPreviewFormat(ImageFormat.JPEG); //useless now...
                mCamera.setParameters(parameters);
                mCamera.setPreviewCallback(mPreviewCallback);
                mCamera.startPreview();
                refreshScreenResolution();
            }
        }
        
        private Camera.PreviewCallback mPreviewCallback = new PreviewCallback(){
            public void onPreviewFrame(byte[] data, Camera camera) {
                
                final int width = mResolution.x;
                final int height = mResolution.y;
                int[] rgb = new int[(width * height)];
                try { 
                    // create bitmap
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    CameraUtil.decodeYUV(rgb, data, width, height);
                    bmp.setPixels(rgb, 0, width, 0, 0, width, height);

                    // recognize faces
                    mFaceDrawer.setResource(bmp);
                    mFaceDrawer.startFaceCatch();
                    mFaceDrawer.invalidate();

                    // release bitmap
                    if (bmp != null){
                        bmp.recycle();
                        bmp = null;
                    }
                } catch (Exception e) { 
                }
            }
        };
        
        private void refreshScreenResolution(){
            int w = mSurface.getWidth();
            int h = mSurface.getHeight();
            mResolution = new Point(w,h);
        }
        
        /**
        // for debug
        private void logAvailablePreviewSize(Camera.Parameters parameters){
             List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
             for (int i=0; i<sizeList.size(); i++){
                 Camera.Size size = sizeList.get(i);
                 Log.e(TAG, "w:"+size.width+" h:"+size.height);
             }
            
        }
        */
    };
}