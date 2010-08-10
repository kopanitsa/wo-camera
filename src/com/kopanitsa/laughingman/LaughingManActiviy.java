package com.kopanitsa.laughingman;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.kopanitsa.common.camera.CameraUtil;

public class LaughingManActiviy extends Activity {
    private static final String TAG = "LaughingManActiviy";
    private static final String FILE_PREFIX = "LaughingMan_";
    private static final String FILE_SUFFIX = ".jpg";

    private Camera mCamera;
    private Point mResolution;
    private FaceDrawerView mFaceDrawer;
    private SurfaceView mSurface;
    private Button mShutter;
    
    private ContentResolver mContentResolver;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mSurface = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder holder = mSurface.getHolder();
        holder.addCallback(mSurfaceListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS  );
        mFaceDrawer = (FaceDrawerView) findViewById(R.id.facedrawer);
        mShutter = (Button) findViewById(R.id.shutter);
        ShutterClickListener shutterListener = new ShutterClickListener();
        mShutter.setOnClickListener(shutterListener);
        
        mContentResolver = getContentResolver();
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
    
    public void takePicture() {
        final Context context = this;
        mCamera.takePicture(null,null,new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data,Camera camera) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    
                    mFaceDrawer.startFaceCatch();
                    addImageAsCamera(mContentResolver, bitmap);
                } catch (Exception e) {
                    Log.e(TAG,""+e.toString());
                }
                mCamera.startPreview();
            }
        }); 
    }

    public static Uri addImageAsCamera(ContentResolver cr, Bitmap bitmap) {  
        long dateTaken = System.currentTimeMillis();  
        String name = FILE_PREFIX + createName(dateTaken) + FILE_SUFFIX;  
        String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name,  
                null);  
        return Uri.parse(uriStr);  
    }  
  
    private static String createName(long dateTaken) {  
        DateFormat df = new SimpleDateFormat("-yyyyMMddHHmmss-");
        Date date = new Date();
        String time = df.format(date);
        return time+dateTaken;
    }  

//    //バイトデータ→SDカード
//    private static void data2sd(Context context,
//        byte[] w,String fileName) throws Exception {
//        //SDカードへのデータ保存
//        FileOutputStream fos=null;
//        try {
//            fos=new FileOutputStream("/sdcard/"+fileName);
//            fos.write(w);
//            fos.close();
//            Log.d(TAG,"picture saved:"+fileName);
//        } catch (Exception e) {
//            if (fos!=null) fos.close();
//            throw e;
//        }
//    }

    
    private class ShutterClickListener implements View.OnClickListener {
        public void onClick(View v) {
            takePicture();
        }
    }
}