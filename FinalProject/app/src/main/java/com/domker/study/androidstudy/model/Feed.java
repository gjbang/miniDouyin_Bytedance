package com.domker.study.androidstudy.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Feed {
    // web's json only have two attribute, one is feeds, the other is success
    @SerializedName("feeds") private List<Video> feeds;
    @SerializedName("success") private boolean success;

    public List<Video> getFeeds(){return feeds;}

    public void setFeeds(List<Video> feeds){this.feeds=feeds;}

    public boolean isSuccess(){return success;}

    public void setSuccess(boolean success){this.success=success;}

}
