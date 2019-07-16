package com.example.minidouyin.model;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Video {

    @SerializedName("student_id")
    private String studentId;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("_id")
    private String _id;
    @SerializedName("video_url")
    private String videoUrl;
    @SerializedName("createdAt")
    private Date createdAt;
    @SerializedName("updatedAt")
    private Date updatedAt;
    @SerializedName("image_w")
    private Integer image_w;
    @SerializedName("image_h")
    private Integer image_h;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setImage_h(Integer image_h) {
        this.image_h = image_h;
    }

    public Integer getImage_h() {
        return image_h;
    }

    public void setImage_w(Integer image_w) {
        this.image_w = image_w;
    }

    public Integer getImage_w() {
        return image_w;
    }
}