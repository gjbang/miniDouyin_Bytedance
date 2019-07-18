package com.domker.study.androidstudy.network;

import okhttp3.RequestBody;


import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


public class ProgressRequestBody extends RequestBody {

    private Uri url;
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
//        void onError();
//        void onFinish();
    }

    public ProgressRequestBody(final File file, final  UploadCallbacks listener) {

        mFile =file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        // i want to upload only images
//        return MediaType.parse("image/*");
//        String type=null;
////        final String url=mFile.toString();
//        final String reurl=mFile.toString();
//        final String extension= MimeTypeMap.getFileExtensionFromUrl(reurl);
//        if(extension!=null){
//            type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
//        }
//        if(type==null){
//            type="*/*";
//        }
//        return MediaType.parse(type);
        return MediaType.parse("multipart/form-data");
    }


    @Override
    public long contentLength() {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int)(100 * (float)mUploaded / (float)mTotal));
        }
    }
}
