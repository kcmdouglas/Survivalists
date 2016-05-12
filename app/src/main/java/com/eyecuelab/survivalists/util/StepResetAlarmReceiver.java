package com.eyecuelab.survivalists.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.services.StepResetIntentService;
import com.firebase.client.Firebase;
import com.google.android.gms.games.Games;

/**
 * Created by eyecuelab on 5/6/16.
 */
public class StepResetAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 1234;
    public static final String ACTION = "com.eyecuelab.survivalists.services.alarm";
    int stepsInCounter;
    int flag=0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();



        int totalSteps = bundle.getInt("endOfDaySteps");
        int dailySteps = bundle.getInt("dailySteps");
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String currentPlayerId = bundle.getString("currentPlayerId");

        Intent resetIntent = new Intent (context, StepResetIntentService.class);
        resetIntent.putExtra("resetDailySteps", 0);
        resetIntent.putExtra("resetPreviousDaySteps", totalSteps);
        resetIntent.putExtra("receiver", receiver);
        context.startService(resetIntent);
    }
}
