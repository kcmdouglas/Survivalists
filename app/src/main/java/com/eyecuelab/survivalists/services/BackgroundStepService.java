package com.eyecuelab.survivalists.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.ui.MainActivity;
import com.eyecuelab.survivalists.util.BackgroundStepReceiver;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eyecuelab on 5/16/16.
 */
public class BackgroundStepService extends Service implements SensorEventListener {

    public BackgroundStepService(Context applicationContext) {
        super();
    }

    public BackgroundStepService() {}

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    int previousDayStepCount;
    String mCurrentPlayerId;
    int dailySteps;
    int mFullnessLevel;
    int mHealthLevel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Initialize SensorManager
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            mSensorManager.registerListener((SensorEventListener) this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        mCurrentPlayerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);

        mEditor.putBoolean(Constants.PREFERENCES_SENSOR_SET_BOOLEAN, true).apply();
        firebaseStatsListener();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.eyecuelab.survivalists.util.BackgroundStepReceiver");
        sendBroadcast(broadcastIntent);
    }

    //STEP SENSOR LOGIC AND FIREBASE CALLS
    @Override
    public void onSensorChanged(SensorEvent event) {
        previousDayStepCount = mSharedPreferences.getInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);

        int stepsInSensor = (int) event.values[0];

        if(stepsInSensor < previousDayStepCount) {
            dailySteps =+ stepsInSensor;
        } else {
            dailySteps = Math.round(event.values[0] - previousDayStepCount);
        }

        mEditor.putInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, stepsInSensor);
        mEditor.putInt(Constants.PREFERENCES_DAILY_STEPS, dailySteps);
        mEditor.commit();
        String dailyStepsString = Integer.toString(dailySteps);

        if((mCurrentPlayerId != null) && (dailySteps % 10 == 0) && (dailySteps > 9)) {
            Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/");
            Map<String, Object> firebaseDailySteps = new HashMap<>();
            firebaseDailySteps.put("dailySteps", dailyStepsString);
            firebaseStepsRef.updateChildren(firebaseDailySteps);
            firebaseStatsListener();
        }

         if((mCurrentPlayerId != null) && (dailySteps % 50 == 0) && (dailySteps > 49)) {
             int newHunger;
             int newHealth;
             if(mFullnessLevel > 0) {
                 newHunger = mFullnessLevel - 1;
                 newHealth = mHealthLevel;
             } else {
                 newHunger = mFullnessLevel;
                 newHealth = mHealthLevel - 1;

             }


             Firebase firebaseCharacterRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/character");
             Map<String, Object> firebaseStatsLevel = new HashMap<>();
             firebaseStatsLevel.put("fullnessLevel", newHunger);
             firebaseStatsLevel.put("health", newHealth);
             firebaseCharacterRef.updateChildren(firebaseStatsLevel);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void firebaseStatsListener() {

        Firebase firebaseCharacterRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/character/");

        firebaseCharacterRef.child("fullnessLevel").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long fullnessLevelLong = (long) dataSnapshot.getValue();
                mFullnessLevel = (int) fullnessLevelLong;
                if (mFullnessLevel < 25 && mFullnessLevel > 16) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    long[] pattern = {0, 300, 0};
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 12345, intent, 0);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(android.R.drawable.sym_def_app_icon)
                            .setContentTitle("Getting pretty hungry...")
                            .setContentText("It'd be wise to get some food before your health starts to decline. Use some food in your inventory to fill your fullness meter.")
                            .setVibrate(pattern)
                            .setAutoCancel(true);

                    mBuilder.setContentIntent(pi);
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                    mBuilder.setAutoCancel(true);
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(12345, mBuilder.build());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        firebaseCharacterRef.child("health").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long healthLevelLong = (long) dataSnapshot.getValue();
                mHealthLevel = (int) healthLevelLong;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
