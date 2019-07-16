package com.example.minidouyin.api;


import com.example.minidouyin.model.Feed;

import retrofit2.Call;
import retrofit2.http.GET;


public interface IMiniDouyinService {
    //http://test.androidcamp.bytedance.com/mini_douyin/invoke/video
    String HOST="http://test.androidcamp.bytedance.com/";   //host
    String PATH="mini_douyin/invoke/video";     //resource path

    @GET(PATH)
    Call<Feed> getFeed();

}
