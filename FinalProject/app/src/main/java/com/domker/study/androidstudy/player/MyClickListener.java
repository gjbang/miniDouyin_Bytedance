package com.domker.study.androidstudy.player;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import android.os.Handler;


public class MyClickListener implements View.OnTouchListener{

    private static int timeout=400;
    private int clickCount=0;
    private Handler handler;

    private MyClickCallBack myClickCallBack;
    public interface MyClickCallBack{
        void oneClick();//点击一次的回调
        void doubleClick();//连续点击两次的回调

    }

    public MyClickListener(MyClickCallBack myClickCallBack) {
        this.myClickCallBack = myClickCallBack;
        handler = new Handler();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            clickCount++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (clickCount == 1) {
                        myClickCallBack.oneClick();
                    }else if(clickCount==2){
                        myClickCallBack.doubleClick();
                    }
                    handler.removeCallbacksAndMessages(null);
                    //清空handler延时，并防内存泄漏
                    clickCount = 0;//计数清零
                }
            },timeout);
        }
        return false;
    }
}

