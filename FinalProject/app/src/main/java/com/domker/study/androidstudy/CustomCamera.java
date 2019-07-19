package com.domker.study.androidstudy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.domker.study.androidstudy.util.ResourceUtils;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.domker.study.androidstudy.util.ResourceUtils.getRealPath;
import static com.domker.study.androidstudy.util.Utils.MEDIA_TYPE_IMAGE;
import static com.domker.study.androidstudy.util.Utils.MEDIA_TYPE_VIDEO;
import static com.domker.study.androidstudy.util.Utils.getOutputMediaFile;
import static com.domker.study.androidstudy.util.Utils.getOutputMediaString;

public class CustomCamera extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;
    private boolean isZoomIn = true;

    private int rotationDegree = 0;
    private float mDist=0;      // camera's zoom distance

    private Handler mHandler=new Handler();
    private File recordFile;
    private String recordStoreString;

    private int delayTime=0;
    private int timeLen=0;

    private FloatingActionButton btn_delay_3,btn_delay_10,btn_time_5,btn_time_10,btn_time_30;
    private Uri mSelectedVideo=null;


    private String userName="";
    private String stuID="";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.d("debug","inSur");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mSurfaceView = findViewById(R.id.img);

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onTouchEvent(event,mCamera);
            }

        });

        Intent intent=getIntent();
        userName=intent.getStringExtra("user_name");
        stuID=intent.getStringExtra("stuid");


//        mSurfaceView.setZOrderOnTop(true);//添加这句代码可以解决黑屏问题，但是SurfaceView上的控件被遮挡了
//        mSurfaceView.setZOrderMediaOverlay(true);//加这句的话解决SurfaceView上的控件都被遮挡的问题
        //to.do 给SurfaceHolder添加Callback
        mCamera = getCamera(CAMERA_TYPE);
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });


        btn_delay_3=findViewById(R.id.time_delay_3);
        btn_delay_10=findViewById(R.id.time_delay_10);
        btn_time_5=findViewById(R.id.time_len_5);
        btn_time_10=findViewById(R.id.time_len_10);
        btn_time_30=findViewById(R.id.time_len_30);

        btn_delay_3.setOnClickListener(click);
        btn_delay_10.setOnClickListener(click);
        btn_time_5.setOnClickListener(click);
        btn_time_10.setOnClickListener(click);
        btn_time_30.setOnClickListener(click);

//        unsigned demo=btn_time_30.getColorNormal();

        Log.d("debug","color"+ new Integer(btn_time_30.getColorNormal()).toString());

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //to.do 拍一张照片
            mCamera.takePicture(null, null, mPicture);
        });

        findViewById(R.id.btn_record).setOnClickListener(v -> {
            //to.do 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //to.do 停止录制
                isRecording = false;
                releaseMediaRecorder();
            } else {
                //to.do 录制
                isRecording = true;
                prepareVideoRecorder();
                try {
                    mMediaRecorder.prepare();

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMediaRecorder.start();
                        }
                    },delayTime);


                }catch (Exception e){
                    mMediaRecorder.release();
                    e.printStackTrace();
                    return;
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            //to.do 切换前后摄像头
            if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_FRONT)
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            else
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            startPreview(mSurfaceHolder);
        });

        findViewById(R.id.btn_from_File).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });

//        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
//            //todo 调焦，需要判断手机是否支持
//            Camera.Parameters parameters = mCamera.getParameters();
//            if(parameters.isZoomSupported()) {
//                int maxZoom = parameters.getMaxZoom();
//                int zoom = parameters.getZoom();
//                if(isZoomIn) {
//                    if(zoom < maxZoom)
//                        zoom++;
//                    else
//                        isZoomIn = false;
//                }
//                else {
//                    if(zoom > 0)
//                        zoom--;
//                    else
//                        isZoomIn = true;
//                }
//                parameters.setZoom(zoom);
//                mCamera.setParameters(parameters);
//            }
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private static final int PICK_VIDEO = 2;

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d("debug", "mSelectedVideo = " + mSelectedVideo);
//                mBtn.setText(R.string.post_it);

                if(mSelectedVideo!=null){
                    Intent intent=new Intent(CustomCamera.this,PostActivity.class);

                    File tempF=new File(ResourceUtils.getRealPath(CustomCamera.this,mSelectedVideo));

                    Log.d("debug",tempF.getAbsolutePath());
                    intent.putExtra("url",tempF.getAbsolutePath());

                    intent.putExtra("userName",userName);
                    intent.putExtra("stuID",stuID);

                    startActivity(intent);
                }
            }
        }
    }

    //-----------------------------------------------------------------//
    //----------------Finger Zoom--------------------------------------//
    public boolean onTouchEvent(MotionEvent event, Camera mC) {
        // Get the pointer ID
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double)(x * x + y * y));
    }

    //-------------------------------Finger zoom-------------------------------//

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        Camera.Parameters params = cam.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        rotationDegree = getCameraDisplayOrientation(CAMERA_TYPE);
        cam.setDisplayOrientation(rotationDegree);
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //to.do 释放camera资源
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //to.do 开始预览
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //to.do 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recordFile=(getOutputMediaFile(MEDIA_TYPE_VIDEO));
            mMediaRecorder.setOutputFile(recordFile);
        }else{
            recordStoreString=getOutputMediaString(MEDIA_TYPE_VIDEO);
            mMediaRecorder.setOutputFile(recordStoreString);
        }

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        mMediaRecorder.setOrientationHint(rotationDegree);

        if(timeLen!=0){
            mMediaRecorder.setMaxDuration(timeLen);
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                        releaseMediaRecorder();
                    }
                }
            });
        }

        return true;
    }


    private void releaseMediaRecorder() {
        //to.do 释放MediaRecorder
        Intent intent=new Intent(CustomCamera.this,PostActivity.class);

        Log.d("debug",recordFile.getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("url",recordFile.getAbsolutePath());
        }else{
            intent.putExtra("url",recordStoreString);
        }

        intent.putExtra("userName",userName);
        intent.putExtra("stuID",stuID);

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();

        startActivity(intent);
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        try {
            ExifInterface srcExif = new ExifInterface(pictureFile.getAbsolutePath());
            switch (rotationDegree){
                case DEGREE_90:
                    srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                    break;
                case DEGREE_180:
                    srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                    break;
                case DEGREE_270:
                    srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                    break;
                default:
                    break;
            }
            srcExif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    };

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private boolean cdelay_3=false,cdelay_10=false,ctime_5=false,ctime_10=false,ctime_30=false;

    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.time_delay_3:
                    if(!cdelay_3){
                        btn_delay_3.setColorNormal(0xA79243E6);
                        btn_delay_10.setColorNormal(0xFFDA4336);
                        cdelay_10=false;
                        cdelay_3=true;

                        delayTime=3000;
                    }
                    else{
                        btn_delay_3.setColorNormal(0xFFDA4336);
                        cdelay_3=false;

                        if(!cdelay_3&&!cdelay_10){
                            delayTime=0;
                        }
                    }

                    break;

                case R.id.time_delay_10:
                    if(!cdelay_10){
                        btn_delay_10.setColorNormal(0xA79243E6);
                        btn_delay_3.setColorNormal(0xFFDA4336);
                        cdelay_3=false;
                        cdelay_10=true;

                        delayTime=10000;
                    }
                    else{
                        btn_delay_10.setColorNormal(0xFFDA4336);
                        cdelay_10=false;

                        if(!cdelay_3&&!cdelay_10){
                            delayTime=0;
                        }
                    }
                    break;

                case R.id.time_len_5:
                    if(!ctime_5){
                        btn_time_5.setColorNormal(0xA3F7559E);
                        ctime_5=true;
                        timeLen=5000;

                        btn_time_10.setColorNormal(0xFFDA4336);
                        btn_time_30.setColorNormal(0xFFDA4336);
                        ctime_10=false;
                        ctime_30=false;
                    }
                    else{
                        btn_time_5.setColorNormal(0xFFDA4336);
                        ctime_5=false;

                        if(!ctime_5&&!ctime_30&&!ctime_10){
                            timeLen=0;
                        }
                    }
                    break;

                case R.id.time_len_10:
                    if(!ctime_10){
                        btn_time_10.setColorNormal(0xA3F7559E);
                        ctime_10=true;
                        timeLen=10000;

                        btn_time_5.setColorNormal(0xFFDA4336);
                        btn_time_30.setColorNormal(0xFFDA4336);
                        ctime_5=false;
                        ctime_30=false;
                    }
                    else{
                        btn_time_10.setColorNormal(0xFFDA4336);
                        ctime_10=false;

                        if(!ctime_5&&!ctime_30&&!ctime_10){
                            timeLen=0;
                        }
                    }
                    break;

                case R.id.time_len_30:
                    if(!ctime_30){
                        btn_time_30.setColorNormal(0xA3F7559E);
                        ctime_30=true;
                        timeLen=30000;

                        btn_time_10.setColorNormal(0xFFDA4336);
                        btn_time_5.setColorNormal(0xFFDA4336);
                        ctime_10=false;
                        ctime_5=false;
                    }
                    else{
                        btn_time_30.setColorNormal(0xFFDA4336);
                        ctime_30=false;

                        if(!ctime_5&&!ctime_30&&!ctime_10){
                            timeLen=0;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
