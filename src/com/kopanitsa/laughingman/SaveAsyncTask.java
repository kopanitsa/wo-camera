package com.kopanitsa.laughingman;

import android.os.AsyncTask;

public class SaveAsyncTask extends AsyncTask<byte[], Integer, Boolean> {
    private static final String TAG = "SaveAsyncTask";
    
    SaveAsyncTaskListener mListener;
    byte[] mData;
    
    public SaveAsyncTask(SaveAsyncTaskListener listener, byte[] data){
        mListener = listener;
        mData = data;
    }
    
    @Override
    protected void onPreExecute() {
        
    }

    @Override
    protected Boolean doInBackground(byte[]... params) {
        mListener.start(mData);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onSaveFinished();
    }
    
    public interface SaveAsyncTaskListener {
        public void start(byte[] data);
        public void onSaveFinished();
    }
}
