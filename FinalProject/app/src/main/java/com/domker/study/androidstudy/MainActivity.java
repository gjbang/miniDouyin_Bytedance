package com.domker.study.androidstudy;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.domker.study.androidstudy.api.IMiniDouyinService;
import com.domker.study.androidstudy.database.DbOperation;
import com.domker.study.androidstudy.database.SQLDbHelper;
import com.domker.study.androidstudy.model.Feed;
import com.domker.study.androidstudy.model.Video;
import com.domker.study.androidstudy.util.ImageHelper;
import com.idescout.sql.SqlScoutServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();

    private SQLiteDatabase database;
    private SQLDbHelper dbHelper;
    private DbOperation dataop;

    private Button btn_camera;

    private Handler mhandler=new Handler();
    private int REQUEST_CODE=123;

    // TODO: purpose: recycle view show videos
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dbHelper=new SQLDbHelper(this);//TODO: 这几行不注释掉会报错
        //dataop=new DbOperation(database,dbHelper);
        //dataop.Initialize();

        btn_camera=findViewById(R.id.btn_for_camera);

//        btn_camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, CustomCamera.class));
//            }
//        });
        btn_camera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]
                                {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_CODE);
            }

            else {
                Log.d("debug","preinSur");
                startActivity(new Intent(MainActivity.this, CustomCamera.class));
            }
        });

        initDatabase();
        mVideos=dataop.loadVideoFromDatabase();
        initRecyclerView();
        //mRv.getAdapter().notifyItemInserted(15);
                //notifyDataSetChanged();
        DataRefresh();
//        mhandler.post(new Runnable() {
//            @Override
//            public void run() {
//                DataRefresh();
//            }
//        });


    }


    public void initDatabase(){
        SqlScoutServer.create(this,getPackageName());

        dbHelper=new SQLDbHelper(this);
        dataop=new DbOperation(database,dbHelper);
        dataop.Initialize();

    }


    @SuppressLint("StaticFieldLeak")
    public void DataRefresh() {

        //use retrofit to get data from server
        //Call<Feed> call=getMiniDouyinService().getFeed();
        Call<Feed> call = getMiniDouyinService().getFeed();

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    //Feed feed=response.body();  //get feed object
                    //List<Video> videos=feed.getFeeds(); //get videos' list

                    // List<Video>即为返回的数据，RecycleView的输入源//DONE:7.16但暂时为直接json
                    mVideos.clear();
                    mVideos=dataop.loadVideoFromDatabase();
                    //mVideos.addAll(response.body().getFeeds());

                    //Objects.requireNonNull(mRv.getAdapter()).notifyItemInserted(15);
                            //notifyDataSetChanged();
                    mRv.getAdapter().notifyDataSetChanged();

//                    List<Video> videos=response.body().getFeeds();
                    dataop.saveVideo2Database(mVideos);

//                    dataop.saveVideo2Database(videos);
                    call.cancel();
                }
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "retrofit: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                if(!call.isCanceled()){
                    call.cancel();
                }
            }
        });

//        return result;
    }


    //TODO:7.17 nochange
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


    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);

        mRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

//
//        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);//可防止Item切换
//        mRv.setLayoutManager(layoutManager);

        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(
                        LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.video_item_fragment, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.bind(MainActivity.this, video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }


        });
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }

        public void bind(final Activity activity, final Video video) {

            //ImageHelper.displayWebImage(video.getImageUrl(), img);
            int width = ((Activity)img.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams para;
            para = img.getLayoutParams();
            para.width = width/2;
            para.height = video.getImage_h()*para.width/video.getImage_w();
            img.setLayoutParams(para);
            Log.d("MainActivity",para.height+" "+para.width);


            img.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(img.getContext()).load(video.getImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.place_holder).into(img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, IJKPlayerActivity.class);
                    intent.putExtra("url", video.getVideoUrl());
                    Log.d("debug",video.getVideoUrl());
                    activity.startActivity(intent);
                }
            });
        }
    }
}
