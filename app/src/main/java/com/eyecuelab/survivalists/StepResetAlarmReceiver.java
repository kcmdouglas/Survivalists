package com.eyecuelab.survivalists;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by eyecuelab on 5/6/16.
 */
public class StepResetAlarmReceiver extends BroadcastReceiver {

    int stepsInCounter;
    int flag=0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        Log.v("Receiver", "Total steps: " + bundle.getInt("endOfDaySteps"));

        int totalSteps = bundle.getInt("endOfDaySteps");

        Toast.makeText(context, "Made it to the broadcast receiver", Toast.LENGTH_LONG).show();
        Log.v("Receiver", " Made it!!");


        Intent resetIntent = new Intent ("resetBroadcast");
        resetIntent.putExtra("resetDailySteps", 0);
        resetIntent.putExtra("resetRecordedStepsCount", totalSteps);
        context.sendBroadcast(resetIntent);
    }
}
