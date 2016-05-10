package com.eyecuelab.survivalists;

import android.app.Application;

import com.firebase.client.Firebase;

public class SurvivalistsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
