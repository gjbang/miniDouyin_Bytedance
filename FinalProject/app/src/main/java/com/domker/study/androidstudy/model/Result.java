package com.domker.study.androidstudy.model;

import com.google.gson.annotations.SerializedName;

public class Result {

    // a class used to define
    @SerializedName("result") private Object result;
    @SerializedName("url") private String url;
    @SerializedName("success") private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

