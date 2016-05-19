package com.eyecuelab.survivalists.services;

import android.app.AlarmManager;
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
import android.util.Log;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.util.BackgroundStepReceiver;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.HashMap;
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
    int fullnessLevel;
    Character playerCharacter;

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
        String userJson = mSharedPreferences.getString(Constants.PREFERENCES_USER, null);
        Gson userGson = new Gson();
        User currentUser = userGson.fromJson(userJson, User.class);
        playerCharacter = currentUser.getPlayedCharacter();
        fullnessLevel = playerCharacter.getFullnessLevel();
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


        if((mCurrentPlayerId != null) && (dailySteps % 10 < 1)) {
            Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + mCurrentPlayerId + "/");
            Map<String, Object> firebaseDailySteps = new HashMap<>();
            firebaseDailySteps.put("daily_steps", dailyStepsString);
            firebaseStepsRef.updateChildren(firebaseDailySteps);
            firebaseStepListener();
        }

//         if((mCurrentPlayerId != null) && (dailySteps % 50 < 1)) {
//
//             Firebase firebaseCharacterRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/character");
//             Map<String, Object> firebaseHungerLevel = new HashMap<>();
//             firebaseHungerLevel.put("fullness_level", fullnessLevel);
//             firebaseCharacterRef.updateChildren(firebaseHungerLevel);
//             firebaseHungerListener();
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void firebaseStepListener() {

        Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + mCurrentPlayerId);
        Query queryRef = firebaseStepsRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Firebase Update", dataSnapshot.getKey());
                Log.d("Firebase Update", dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    private void firebaseHungerListener() {

        Firebase firebaseCharacterRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/character");
        Query queryRef = firebaseCharacterRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Firebase Update", dataSnapshot.getKey());
                Log.d("Firebase Update", dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }


//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if(key.equals(Constants.PREFERENCES_PREVIOUS_STEPS_KEY)) {
//            previousDayStepCount = mSharedPreferences.getInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);
//        }
//
//    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
