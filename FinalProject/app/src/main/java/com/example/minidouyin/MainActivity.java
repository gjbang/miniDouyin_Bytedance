package com.example.minidouyin;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.domker.study.androidstudy.R;
import com.example.minidouyin.api.IMiniDouyinService;
import com.example.minidouyin.database.DbOperation;
import com.example.minidouyin.database.SQLDbHelper;
import com.example.minidouyin.model.Feed;
import com.example.minidouyin.model.Video;

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

        dbHelper=new SQLDbHelper(this);
        dataop=new DbOperation(database,dbHelper);
        dataop.Initialize();

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
        // TODO 10: get videos & update recycler list

        //use retrofit to get data from server
        //Call<Feed> call=getMiniDouyinService().getFeed();
        Call<Feed> call = getMiniDouyinService().getFeed();

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    Feed feed=response.body();  //get feed object
                    List<Video> videos=feed.getFeeds(); //get videos' list

                        //TODO: List<Video>即为返回的数据，RecycleView的输入源

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
}
