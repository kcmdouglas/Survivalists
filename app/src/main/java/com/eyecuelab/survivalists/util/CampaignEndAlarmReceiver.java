package com.eyecuelab.survivalists.util;

/**
 * Created by eyecuelab on 5/11/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.eyecuelab.survivalists.services.CampaignEndAlarmService;

public class CampaignEndAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 01234;
    public static final String ACTION = "com.eyecuelab.survivalists.services.alarm";
    int stepsInCounter;
    int flag=0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();


        Intent endCampaign = new Intent (context, CampaignEndAlarmService.class);
        endCampaign.putExtras(bundle);
        context.startService(endCampaign);
    }
}
