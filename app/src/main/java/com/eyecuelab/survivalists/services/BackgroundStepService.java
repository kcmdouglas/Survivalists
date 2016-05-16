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
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Initialize SensorManager
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            mSensorManager.registerListener((SensorEventListener) this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

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
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEditor= mSharedPreferences.edit();

        String mCurrentPlayerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);
        int previousDayStepCount = mSharedPreferences.getInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);
        int stepsInSensor = (int) event.values[0];
        int dailySteps;

        if(stepsInSensor < previousDayStepCount) {
            dailySteps =+ stepsInSensor;
        } else {
            dailySteps = Math.round(event.values[0] - previousDayStepCount);
        }

        mEditor.putInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, stepsInSensor);
        mEditor.putInt(Constants.PREFERENCES_DAILY_STEPS, dailySteps);
        mEditor.commit();

//        dailyCounter.setText(Integer.toString(dailySteps));
//        counter.setText(Integer.toString(stepsInSensor));
        String dailyStepsString = Integer.toString(dailySteps);


        initiateDailyCountResetService(stepsInSensor);

        if((mCurrentPlayerId != null) && (dailySteps % 10 < 1)) {
            Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + mCurrentPlayerId + "/");
            Map<String, Object> firebaseDailySteps = new HashMap<>();
            firebaseDailySteps.put("daily_steps", dailyStepsString);
            firebaseStepsRef.updateChildren(firebaseDailySteps);
            firebaseStepListener();
        }

//        if ((mCurrentMatchId != null) && (mNextSafehouse.reachedSafehouse(dailySteps))) {
//            Toast.makeText(MainActivity.this, "You've reached " + mNextSafehouse.getHouseName(), Toast.LENGTH_SHORT).show();
//            Toast.makeText(MainActivity.this, mNextSafehouse.getDescription(), Toast.LENGTH_LONG).show();
//        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void firebaseStepListener() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mCurrentPlayerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);

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

    public void initiateDailyCountResetService(int stepsInSensor) {
        //Bundles the number of steps in the sensor
        Intent intent = new Intent(this, StepResetAlarmReceiver.class);
        Bundle bundle = new Bundle();

        bundle.putInt("endOfDaySteps", stepsInSensor);

        intent.putExtras(bundle);
        //Sets a recurring alarm just before midnight daily to trigger BroadcastReceiver
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getBroadcast(this, StepResetAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
