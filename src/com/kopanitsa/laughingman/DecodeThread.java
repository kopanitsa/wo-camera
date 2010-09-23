package com.kopanitsa.laughingman;

import java.util.concurrent.ArrayBlockingQueue;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;

import com.kopanitsa.common.camera.CameraUtil;

public class DecodeThread extends Thread {
    private static final String TAG = "DecodeThread";

    private FaceDrawerView mFaceDrawer;
    private Point mResolution;
    private boolean mShouldDecode = false;

    private ArrayBlockingQueue<byte[]> mQueue = new ArrayBlockingQueue<byte[]>(1);
    private Handler mHandler = new Handler();

    public void setFaceDrawer(FaceDrawerView faceDrawer) {
        Log.d(TAG,"setFaceDrawer:"+faceDrawer);
        mFaceDrawer = faceDrawer;
    }

    public void setResolution(Point resolution) {
        Log.d(TAG,"setResolution:"+resolution);
        mResolution = resolution;
    }

    public boolean testQueueIsEmpty() {
        int length = 0;
        length = mQueue.size();
        return length==0;
    }

    public void setData(byte[] data) {
        Log.d(TAG,"setData");
        if (mQueue.size()==0){
            mQueue.add(data);
        }
    }

    public void shouldDecode(boolean shouldDecode){
        Log.d(TAG,"shouldDecode:"+shouldDecode);
        mShouldDecode = shouldDecode;
    }

    @Override
    public void start(){
        Log.d(TAG,"start");
        mShouldDecode = true;
        super.start();
    }

    @Override
    public void run(){
        boolean shouldDecode = true;
        while (shouldDecode){
            shouldDecode = mShouldDecode;
            if (!testQueueIsEmpty()){
                byte[] data = null;
                data = mQueue.peek();
                Log.d(TAG,"decode -s");
                decode(data);
                Log.d(TAG,"decode -e");
                mQueue.remove();
            }
        }
    }

    public void decode(byte[] data){
        //        Debug.startMethodTracing("laugh6_native");
        if (mFaceDrawer == null){
            Log.d(TAG,"mFaceDrawer is null");
            return;
        }
        if (mResolution == null){
            Log.d(TAG,"mResolution is null");
            return;
        }

        final int width = mResolution.x;
        final int height = mResolution.y;
        int[] rgb = new int[(width * height)];

        // create bitmap
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        CameraUtil.decodeYUV_fast(rgb, data, width, height);
        bmp.setPixels(rgb, 0, width, 0, 0, width, height);

        // recognize faces
        mFaceDrawer.setResource(bmp);
        mFaceDrawer.startFaceCatch();
        mHandler.post(new Runnable() {
            public void run() {
                mFaceDrawer.invalidate();
            }
        });

        // release bitmap
        if (bmp != null){
            bmp.recycle();
            bmp = null;
        }
        calcDecodeSpeed();
        //        Debug.stopMethodTracing();
    }

    // DEBUG -----------------------------------------------------------------

    private int counter = 0;
    private long time = 0L;
    private float num = 10.f;
    private void calcDecodeSpeed(){
        counter++;
        if (counter==(int)num){
            long tmp = System.currentTimeMillis();
            long diff = tmp - time;
            time = System.currentTimeMillis();
            float dps = 1000.f*num/(float)diff;
            Log.d(TAG,"DecodePerSec:"+dps);
            counter = 0;
        }
    }
}
