package com.example.jinbailiang.albumdemo_jin;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class PhotoApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
    }
}
