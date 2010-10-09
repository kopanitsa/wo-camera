package com.kopanitsa.laughingman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class LaughingManActiviy extends Activity {
    private static final String TAG = "LaughingManActiviy";
    private static final String FILE_PREFIX = "LaughingMan_";
    private static final String FILE_SUFFIX = ".jpg";

    private static final String APPLICATION_NAME = "Laughingman";  
    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + APPLICATION_NAME;

    private Camera mCamera;
    private Point mResolution;
    private FaceDrawerView mFaceDrawer;
    private SurfaceView mSurface;
    private Button mShutter;
    private ImageView mSavingView;
    
    private ContentResolver mContentResolver;
    private DecodeThread mDecodeThread;
    private FocusListener mFocusListener = new FocusListener();

    private boolean mSaving = false;
    private boolean mFocusButtonPressed = false;
    
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
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mFaceDrawer = (FaceDrawerView) findViewById(R.id.facedrawer);
        mShutter = (Button) findViewById(R.id.shutter);
        ShutterClickListener shutterListener = new ShutterClickListener();
        mShutter.setOnClickListener(shutterListener);
        mSavingView = (ImageView) findViewById(R.id.save_image);
        mSavingView.setVisibility(View.GONE);
        mContentResolver = getContentResolver();
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        android.util.Log.e(TAG,"action:"+event.getAction()+" key:"+event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_FOCUS:
                if (!mFocusButtonPressed){
                    mCamera.autoFocus(null);
                    mFocusButtonPressed = true;
                }
                break;
            case KeyEvent.KEYCODE_CAMERA:
                if(!mSaving){
                    mSaving = true;
                    takePicture();
                    mSavingView.setVisibility(View.VISIBLE);
                }
                return true;
            default:
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP){
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_FOCUS:
                mFocusButtonPressed = false;
                break;
            default:
            }
        }
        return super.dispatchKeyEvent(event);
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
                
                parameters.setPreviewSize(w, h);
                parameters.setPictureSize(1024, 768); // for nexus one
                
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
                saveImage(data);
                mCamera.startPreview();
                mSavingView.setVisibility(View.GONE);
                
                mSaving = false;
            }
        }); 
    }
    
    public void saveImage(byte[] data){
        long start = System.currentTimeMillis();
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Drawable mask = mFaceDrawer.getMaskImage();
            addImageWithMask(mContentResolver, bitmap, mask);
        } catch (Exception e) {
            Log.e(TAG,""+e.toString());
        }
        long diff = System.currentTimeMillis() - start;
        Log.e(TAG,"saving time:"+diff);
    }
    
//    private SaveAsyncTask.SaveAsyncTaskListener mSaveTaskListener 
//        = new SaveAsyncTask.SaveAsyncTaskListener(){
//        public void start(byte[] data){
//            saveImage(data);
//        }
//
//        public void onSaveFinished() {
//            mSaving = false;
//            mSavingView.setVisibility(View.GONE);
//            mCamera.startPreview();
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
    
    private static Uri save(ContentResolver cr, Bitmap bitmap){
        long dateTaken = System.currentTimeMillis();  
        String name = FILE_PREFIX + createName(dateTaken) + FILE_SUFFIX;
        return addImage(cr, name, dateTaken, PATH, name, bitmap, null);
    }
    
    private static Uri addImage(ContentResolver cr, String name, long dateTaken, String directory,  
            String filename, Bitmap source, byte[] jpegData) {  
      
        OutputStream outputStream = null;  
        String filePath = directory + "/" + filename;  
        try {  
            File dir = new File(directory);  
            if (!dir.exists()) {  
                dir.mkdirs();  
                Log.d(TAG, dir.toString() + " create");  
            }  
            File file = new File(directory, filename);  
            if (file.createNewFile()) {  
                outputStream = new FileOutputStream(file);  
                if (source != null) {  
                    source.compress(CompressFormat.JPEG, 75, outputStream);  
                } else {  
                    outputStream.write(jpegData);  
                }  
            }  
      
        } catch (FileNotFoundException ex) {  
            Log.w(TAG, ex);  
            return null;  
        } catch (IOException ex) {  
            Log.w(TAG, ex);  
            return null;  
        } finally {  
            if (outputStream != null) {  
                try {  
                    outputStream.close();  
                } catch (Throwable t) {  
                }  
            }  
        }  
          
        ContentValues values = new ContentValues(7);  
        values.put(Images.Media.TITLE, name);  
        values.put(Images.Media.DISPLAY_NAME, filename);  
        values.put(Images.Media.DATE_TAKEN, dateTaken);  
        values.put(Images.Media.MIME_TYPE, "image/jpeg");  
        values.put(Images.Media.DATA, filePath);  
        return cr.insert(IMAGE_URI, values);  
    }  
  
    private static String createName(long dateTaken) {  
        DateFormat df = new SimpleDateFormat("-yyyyMMddHHmmss-");
        Date date = new Date();
        String time = df.format(date);
        return time+dateTaken;
    }  

    private class ShutterClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if(!mSaving){
                mSaving = true;
                mCamera.autoFocus(mFocusListener);
            }
        }
    }
    
    private class FocusListener implements Camera.AutoFocusCallback{
        public void onAutoFocus(boolean success, Camera camera) {
            takePicture();
            mSavingView.setVisibility(View.VISIBLE);
        }
    }
}