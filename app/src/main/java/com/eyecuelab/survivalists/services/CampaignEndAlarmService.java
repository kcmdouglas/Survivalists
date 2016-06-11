package com.eyecuelab.survivalists.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.ui.MainActivity;
import com.eyecuelab.survivalists.ui.TitleActivity;

/**
 * Created by eyecuelab on 5/19/16.
 */
public class CampaignEndAlarmService extends Service {
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, false);
        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);
        mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, -1);
        mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, -1);
        mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, -1);
        mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, -1);
        mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);
        mEditor.putBoolean(Constants.PREFERENCES_INITIALIZE_GAME_BOOLEAN, false);
        mEditor.putBoolean(Constants.PREFERENCES_COMPLETED_CAMPAIGN_BOOLEAN, true);
        mEditor.putString("matchId", null);
        mEditor.apply();
        Log.d("Here I am???", "In the alarm service??");
        Intent intent = new Intent(this, TitleActivity.class);
        long[] pattern = {0, 300, 0};
        PendingIntent pi = PendingIntent.getActivity(this, 12345, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle("Your Campaign has Completed!")
                .setContentText("Was your campaign successful? Did you meet friends along the way?")
                .setVibrate(pattern)
                .setAutoCancel(true);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(12345, mBuilder.build());
    }
}
