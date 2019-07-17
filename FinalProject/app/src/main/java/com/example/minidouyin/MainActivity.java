package com.example.minidouyin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.example.minidouyin.api.IMiniDouyinService;
import com.example.minidouyin.database.DbOperation;
import com.example.minidouyin.database.SQLDbHelper;
import com.example.minidouyin.model.Feed;
import com.example.minidouyin.model.Video;
import com.example.minidouyin.util.ImageHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

//    private TextView textView;
//    private Button button;
//    private String result;

    private SQLiteDatabase database;
    private SQLDbHelper dbHelper;
    private DbOperation dataop;

    // TODO: purpose: recycle view show videos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dbHelper=new SQLDbHelper(this);//TODO: 这几行不注释掉会报错
        //dataop=new DbOperation(database,dbHelper);
        //dataop.Initialize();

        initRecyclerView();
        fetchFeed();//不适用fetch的按键，而是一加载就获取数据

//        textView=findViewById(R.id.test_text);
//        button=findViewById(R.id.btn_test);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                textView.setText(fetchFeed());
//            }
//        });
    }


    @SuppressLint("StaticFieldLeak")
    public void fetchFeed() {

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
                    mVideos = response.body().getFeeds();
                    mRv.getAdapter().notifyDataSetChanged();

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
                Log.d("MainActivity","------"+video.getVideoUrl()+" "+video.getImageUrl());
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
            //img.setMaxHeight((int)(200+Math.random()*400));
        }

        public void bind(final Activity activity, final Video video) {
//            ImageHelper.displayWebImage(video.getImageUrl(), img);//todo:这里是读取图片，而应该读取视频的一帧作为封面


            Glide.with(img.getContext()).load(video.getImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_launcher_background).crossFade().into(img);
            //img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setScaleType(ImageView.ScaleType.FIT_START);
            //img.setScaleX(video.getImage_w());
            //img.setScaleY(video.getImage_h());
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerActivity.launch(activity, video.getVideoUrl());
                    Log.d("MainActivity","-click---"+video.getVideoUrl()+" "+video.getImageUrl());
                }
            });
        }

        private Bitmap getNetVideoBitmap(String videoUrl) {
            Bitmap bitmap = null;

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                //根据url获取缩略图
                retriever.setDataSource(videoUrl, new HashMap());
                //获得第一帧图片
                bitmap = retriever.getFrameAtTime();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                retriever.release();
            }
            return bitmap;
        }
    }
}
