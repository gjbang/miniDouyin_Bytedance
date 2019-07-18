package com.domker.study.androidstudy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.domker.study.androidstudy.api.IMiniDouyinService;
import com.domker.study.androidstudy.model.Result;
import com.domker.study.androidstudy.util.ResourceUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.domker.study.androidstudy.network.*;
import com.github.clans.fab.FloatingActionButton;

public class PostActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks{

    private final String TAG = "main";
    private SurfaceView sv;
    private FloatingActionButton btn_choose,btn_post,btn_choose_image;

    private AppCompatImageButton btn_back;
    private ImageView view_show_image=null;
    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;
    private boolean isPlaying;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    private Retrofit retrofit;
    public ProgressDialog progressDialog;
    private IMiniDouyinService miniDouyinService;

    String url;

    private boolean afterChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent=getIntent();
        url=intent.getStringExtra("url");

        Log.d("debug","url1"+url);

        sv = (SurfaceView) findViewById(R.id.post_ijkPlayer);
        btn_post=findViewById(R.id.post_do_post);
        btn_choose_image=findViewById(R.id.post_choose);
        btn_back=findViewById(R.id.post_back);
        view_show_image=findViewById(R.id.preview_image);

        btn_post.setOnClickListener(click);
        btn_choose_image.setOnClickListener(click);
        btn_back.setOnClickListener(click);


        // 为SurfaceHolder添加回调
        sv.getHolder().addCallback(callback);
        Log.d("debug","url2"+url);

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

    }



//    @Override
//    protected void onResume() {
//        super.onResume();
//        play(0);
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        play(0);
//    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                sv.setVisibility(View.GONE);
                view_show_image.setImageURI(mSelectedImage);
                view_show_image.setVisibility(View.VISIBLE);
                Log.d(TAG, "selectedImage = " + mSelectedImage);
//                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
//                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(PostActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    // post data to server
    private void postVideo() {
//        mBtn.setText("POSTING...");
//        mBtn.setEnabled(false);
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        // MultipartBody.Part videoPart = getMultipartFromUri("video" , mSelectedVideo);
        // TODO 9: post video & update buttons

        File videoFile=new File(url);
        MultipartBody.Part videoPart=MultipartBody.Part.createFormData("video",videoFile.getName(),new ProgressRequestBody(videoFile,this));

        //initialize data interface
        final Call<Result> call=getMiniDouyinService().createVideo("123456",
                "Test",coverImagePart,videoPart);


        progressDialog.setTitle("Uploading");
        progressDialog.setCancelable(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
                call.cancel();
            }
        });
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // whether successful or not, refresh button state
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Toast.makeText(getApplicationContext(),"Success!"+response.isSuccessful(),Toast.LENGTH_SHORT).show();
//                mBtn.setText(R.string.select_an_image);
//                mBtn.setEnabled(true);
                call.cancel();
                progressDialog.setProgress(100);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable throwable) {
//                mBtn.setText(R.string.select_an_image);
//                mBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(),throwable.getMessage(),Toast.LENGTH_SHORT).show();
                if(!call.isCanceled()){
                    call.cancel();
                }

            }
        });

        Toast.makeText(this, "TODO 9: post video & update buttons", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onProgressUpdate(int percentage) {
        progressDialog.setProgress(percentage);
    }

    // initialize retrofit
    private IMiniDouyinService getMiniDouyinService(){
        if(retrofit==null){

            //set retry connection and corresponding timeout use okhttp
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(120,TimeUnit.SECONDS)
                    .writeTimeout(120,TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();


            retrofit=new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        if(miniDouyinService==null){
            miniDouyinService=retrofit.create(IMiniDouyinService.class);
        }

        return miniDouyinService;
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被销毁");
            // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被创建");
            if (currentPosition > 0) {
                // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
                play(currentPosition);
                currentPosition = 0;
            }
            play(0);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(TAG, "SurfaceHolder 大小被改变");
        }

    };


    private View.OnClickListener click = new View.OnClickListener() {

        @SuppressLint("StaticFieldLeak")
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.post_choose:
                    chooseImage();
                    break;
                case R.id.post_do_post:
                    postVideo();
                    break;
                case R.id.post_back:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
    /*1
     * 停止播放
     */
    protected void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    /**
     * 开始播放
     *
     * @param msec 播放初始位置
     */
    protected void play(final int msec) {
        // 获取视频文件地址
        Log.d("debug","url"+url);
        String path = url;
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
            mediaPlayer.setDataSource(file.getAbsolutePath());
            // 设置显示视频的SurfaceHolder
            mediaPlayer.setDisplay(sv.getHolder());
            Log.i(TAG, "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "装载完成");
                    mediaPlayer.start();
                    // 按照初始位置播放
                    mediaPlayer.seekTo(msec);
                    // 设置进度条的最大进度为视频流的最大播放时长

                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    mediaPlayer.start();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    play(0);
                    isPlaying = false;
                    return false;
                }
            });

            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    int videoWidth=mp.getVideoWidth();
                    int videoHeight=mp.getVideoHeight();

                    int surfaceWidth=sv.getWidth();
                    int surfaceHeight=sv.getHeight();

                    float max;
                    if (getResources().getConfiguration().orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        //竖屏模式下按视频宽度计算放大倍数值
                        max = Math.max((float) videoWidth / (float) surfaceWidth,(float) videoHeight / (float) surfaceHeight);
                    } else{
                        //横屏模式下按视频高度计算放大倍数值
                        max = Math.max(((float) videoWidth/(float) surfaceHeight),(float) videoHeight/(float) surfaceWidth);
                    }

                    //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
                    videoWidth = (int) Math.ceil((float) videoWidth / max);
                    videoHeight = (int) Math.ceil((float) videoHeight / max);

                    //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
                    RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(videoWidth,videoHeight);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    sv.setLayoutParams(params);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 重新开始播放
     */
    protected void replay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            Toast.makeText(this, "重新播放", Toast.LENGTH_SHORT).show();
            return;
        }
        isPlaying = false;
        play(0);

    }

    /**
     * 暂停或继续
     */
    protected void pause() {

    }


}
