package com.example.kelly.myapplication;

import android.app.Activity;
import android.content.Intent;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends Activity {
    //private static MediaPlayer media_bg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //AudioInit();

    }
    /*@Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        media_bg.stop();
    }*/

    public void startGame(View view) {
        //开始游戏
        //进入游戏界面
        Intent it = new Intent(this, GameActivity.class);
        startActivity(it);

    }

    /*public void AudioInit() {

        media_bg = MediaPlayer.create(this, R.raw.abc);
        media_bg.start();
    }

    protected void finalize() throws java.lang.Throwable{
        super.finalize();
        media_bg.stop();
    }*/

}