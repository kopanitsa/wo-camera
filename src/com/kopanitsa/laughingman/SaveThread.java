package com.kopanitsa.laughingman;

import com.kopanitsa.laughingman.SaveAsyncTask.SaveAsyncTaskListener;

public class SaveThread extends Thread {

    SaveAsyncTaskListener mListener;
    byte[] mData;

    public SaveThread(SaveAsyncTaskListener listener){
        mListener = listener;
    }
    
    public void setData(byte[] data){
        mData = data;
    }

    @Override
    public void run(){
        mListener.start(mData);
        mListener.onSaveFinished();
    }
}
