package com.domker.study.androidstudy;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private Button btn_about_me;
    private ImageButton btn_refresh;

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;
    private RecyclerView mRv;
    private TextView mMainPage;
    private List<Video> mVideos = new ArrayList<>();

    private SQLiteDatabase database;
    private SQLDbHelper dbHelper;
    private DbOperation dataop;

    private ImageButton btn_camera;

    private Handler mhandler=new Handler();
    private int REQUEST_CODE=123;

    // TODO: purpose: recycle view show videos
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mMainPage = findViewById(R.id.main_page);
//        mAboutMe = findViewById(R.id.about_me);

        mMainPage.setTextColor(getResources().getColor(R.color.white_text));
//        mAboutMe.setTextColor(getResources().getColor(R.color.colorPrimary));

        btn_about_me=findViewById(R.id.about_me);
        btn_about_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLog(v);
            }
        });



        btn_refresh = findViewById(R.id.refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initDatabase();
                mVideos=dataop.loadVideoFromDatabase();

                initRecyclerView();

                new AsyncTask<Objects,Integer,Objects>(){

                    @Override
                    protected Objects doInBackground(Objects... objects) {
                        DataRefresh();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Objects objects) {
                        super.onPostExecute(objects);
                        mVideos=dataop.loadVideoFromDatabase();
                    }
                }.execute();
            }
        });



        btn_camera=findViewById(R.id.btn_for_camera);

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
               if(userName.equals("") || stuID.equals("")){
                 UserLog(v);
               }
               else{
                   Intent intent=new Intent(MainActivity.this,CustomCamera.class);
                   intent.putExtra("user_name",userName);
                   intent.putExtra("stuid",stuID);

                   startActivity(intent);
               }
            }
        });

        initDatabase();
        mVideos=dataop.loadVideoFromDatabase();

        initRecyclerView();


            new AsyncTask<Objects,Integer,Objects>(){

                @Override
                protected Objects doInBackground(Objects... objects) {
                    DataRefresh();
                    return null;
                }

                @Override
                protected void onPostExecute(Objects objects) {
                    super.onPostExecute(objects);
                    mVideos=dataop.loadVideoFromDatabase();
                }
            }.execute();

    }


    public void initDatabase(){
        SqlScoutServer.create(this,getPackageName());

        dbHelper=new SQLDbHelper(this);
        dataop=new DbOperation(database,dbHelper);
        dataop.Initialize();

//        DataRefresh();

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

                    // List<Video>即为返回的数据，RecycleView的输入源//DONE:7.16但暂时为直接json
//                    mVideos.clear();
//                    mVideos=dataop.loadVideoFromDatabase();
//                    mVideos.addAll(response.body().getFeeds());

                    //Objects.requireNonNull(mRv.getAdapter()).notifyItemInserted(15);
                            //notifyDataSetChanged();
//                    mRv.getAdapter().notifyDataSetChanged();

                    dataop.saveVideo2Database(response.body().getFeeds());

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
        public TextView txName;
        public TextView txTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            txName = itemView.findViewById(R.id.txName);
            txTime = itemView.findViewById(R.id.txTime);
        }

        public void bind(final Activity activity, final Video video) {

            int width = ((Activity)img.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams para;
            para = img.getLayoutParams();
            para.width = width/2;
            para.height = video.getImage_h()*para.width/video.getImage_w();
            img.setLayoutParams(para);
            Log.d("MainActivity",para.height+" "+para.width);


            img.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(img.getContext()).load(video.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.place_holder).into(img);
            txName.setText(video.getUserName());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
            String dateString = simpleDateFormat.format(video.getCreatedAt());
            txTime.setText(dateString);

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

    public void UserLog(View view) {
        getInfoOfUser();
    }

    private String userName="";
    private String stuID="";

    private void getInfoOfUser(){
        View dialogView=View.inflate(MainActivity.this,R.layout.layout_userlog,null);

        final EditText editUserName=dialogView.findViewById(R.id.edit_input_user_name);
        final EditText editStuID=dialogView.findViewById(R.id.edit_input_student_ID);

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Log");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userName=editUserName.getText().toString();
                stuID=editStuID.getText().toString();
                if(userName.equals("")||stuID.equals("")){
                    Toast.makeText(MainActivity.this,"Content cannot be null!", Toast.LENGTH_SHORT).show();
                }
                else{

                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userName="";
                stuID="";
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog=builder.create();
        alertDialog.setView(dialogView);
        alertDialog.show();
    }
}
