package com.domker.study.androidstudy.api;





import com.domker.study.androidstudy.model.Feed;
import com.domker.study.androidstudy.model.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface IMiniDouyinService {
    //http://test.androidcamp.bytedance.com/mini_douyin/invoke/video
    String HOST="http://test.androidcamp.bytedance.com/";   //host
    String PATH="mini_douyin/invoke/video";     //resource path

    @GET(PATH)
    Call<Feed> getFeed();

    // post one single video's data
    @Multipart
    @POST(PATH)
    Call<Result> createVideo(
            @Query("student_id") String stuParam,
            @Query("user_name") String userParam,
            @Part MultipartBody.Part coverImage,
            @Part MultipartBody.Part video
    );
}
