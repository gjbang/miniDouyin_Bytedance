package com.domker.study.androidstudy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class PlayerActivity extends AppCompatActivity {

    //TODO: purpose: play specific video //Done: 7.16, 使用hw5的播放器


    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);
    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_player);
//        String url = getIntent().getStringExtra("url");
//        VideoView videoView = findViewById(R.id.video_container);
//        final ProgressBar progressBar = findViewById(R.id.progress_bar);
//        videoView.setMediaController(new MediaController(this));
//        videoView.setVideoURI(Uri.parse(url));
//        videoView.requestFocus();
//        videoView.start();
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                progressBar.setVisibility(View.GONE);
//            }
//        });
//        progressBar.setVisibility(View.VISIBLE);
//    }
}