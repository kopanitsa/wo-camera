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
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

public class LaughingManActiviy extends Activity {
    private static final String TAG = "LaughingManActiviy";
    private static final String FILE_PREFIX = "LaughingMan_";
    private static final String FILE_SUFFIX = ".jpg";

    private Camera mCamera;
    private Point mResolution;
    private FaceDrawerView mFaceDrawer;
    private SurfaceView mSurface;
    private Button mShutter;
    private ProgressBar mProgress;
    private String mModel;
    private String mDevice;
    
    private ContentResolver mContentResolver;
    private DecodeThread mDecodeThread;
    
    private boolean mSaving = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mModel = Build.MODEL;
        mDevice = Build.DEVICE;
        
        mSurface = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder holder = mSurface.getHolder();
        holder.addCallback(mSurfaceListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mFaceDrawer = (FaceDrawerView) findViewById(R.id.facedrawer);
        mShutter = (Button) findViewById(R.id.shutter);
        ShutterClickListener shutterListener = new ShutterClickListener();
        mShutter.setOnClickListener(shutterListener);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.GONE);
        mContentResolver = getContentResolver();
    }
    

    Context mContext = this;
    private SurfaceHolder.Callback mSurfaceListener = 
        new SurfaceHolder.Callback() {
    
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(holder);
                
                mDecodeThread = new DecodeThread();
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
            if (mDecodeThread!=null){
                mDecodeThread.shouldDecode(false);
            }
        }
        
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if(mCamera != null){
                Camera.Parameters parameters = mCamera.getParameters();
                
                // In XPERIA, I cannot set picture size correctly.
                // When preview size is changed, PICTURE size also changed and 
                // cannot handle correctly...
//                if (mDevice.indexOf("SonyEricsson") == -1){
                if (true){
                    parameters.setPreviewSize(w, h);
                    Log.e(TAG,"w:"+w+" h:"+h);
//                    parameters.setPictureSize(1024, 768);
                    parameters.setPictureSize(640, 480);
                }
                
                //parameters.setPreviewFormat(ImageFormat.JPEG); //useless now...
                mCamera.setParameters(parameters);

                parameters = mCamera.getParameters();
                
                // ---- xperia cannot handle one shot preview ? ---
//                mCamera.setOneShotPreviewCallback(mPreviewCallback);
                mCamera.setPreviewCallback(mPreviewCallback);
                // ---- xperia cannot handle one shot preview ? ---

                mCamera.startPreview();
                refreshScreenResolution();
            }
            if (mDecodeThread!=null){
                mDecodeThread.setFaceDrawer(mFaceDrawer);
                mDecodeThread.setResolution(mResolution);
                mDecodeThread.start();
            }
        }
        
        private Camera.PreviewCallback mPreviewCallback = new PreviewCallback(){
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(mDecodeThread.testQueueIsEmpty()){
                    mDecodeThread.setData(data);
                }
                // ---- xperia cannot handle one shot preview ? ---
                //mCamera.setOneShotPreviewCallback(mPreviewCallback);
                // ---- xperia cannot handle one shot preview ? ---
            }
        };

        private void refreshScreenResolution(){
            int w = mSurface.getWidth();
            int h = mSurface.getHeight();
            mResolution = new Point(w,h);
        }
    };
    
    public void takePicture() {
        mCamera.takePicture(null,null,new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data,Camera camera) {
                long start = System.currentTimeMillis();
                mProgress.setVisibility(View.VISIBLE);
                saveImage(data);
                mCamera.startPreview();
                mProgress.setVisibility(View.GONE);
                long diff = System.currentTimeMillis() - start;
                
                Log.e(TAG,"saving time:"+diff);
                mSaving = false;
//                Debug.stopMethodTracing();
            }
        }); 
    }
    
    public void saveImage(byte[] data){
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Drawable mask = mFaceDrawer.getMaskImage();
            addImageWithMask(mContentResolver, bitmap, mask);
        } catch (Exception e) {
            Log.e(TAG,""+e.toString());
        }
    }
    
//    private SaveAsyncTask.SaveAsyncTaskListener mSaveTaskListener 
//    = new SaveAsyncTask.SaveAsyncTaskListener(){
//        public void start(byte[] data){
//            saveImage(data);
//        }
//
//        public void onSaveFinished() {
//            Log.e(TAG,"onSaveFinished()******************");
//            mSaving = false;
//            mProgress.setVisibility(View.GONE);
////            mCamera.startPreview();
//        }
//    };

    public Uri addImageWithMask(ContentResolver cr, Bitmap src, Drawable mask) {  
        Uri uri = null;
        if(src != null){
            // add mask
            Canvas canvas = new Canvas();
            Bitmap bitmap = src.copy(src.getConfig(), true); // src is immutable
            canvas.setBitmap(bitmap);

            FaceCatcher face = new FaceCatcher(bitmap);
            

            FaceCatcher.drawImageToCanvas(canvas, mask, face);
            // save
            uri =  save(cr, bitmap);
            bitmap.recycle();
            bitmap = null;
        }

        return uri;
    }
    
    public static Uri save(ContentResolver cr, Bitmap bitmap){
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

    private class ShutterClickListener implements View.OnClickListener {
        FocusListener focusListener = new FocusListener();
        public void onClick(View v) {
            if(!mSaving){
                mSaving = true;
                mCamera.autoFocus(focusListener);
            }
        }
    }
    
    private class FocusListener implements Camera.AutoFocusCallback{
        public void onAutoFocus(boolean success, Camera camera) {
            takePicture();
        }
    }
}