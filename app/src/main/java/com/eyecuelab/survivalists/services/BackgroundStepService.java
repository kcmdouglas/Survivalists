package com.eyecuelab.survivalists.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by eyecuelab on 5/16/16.
 */
public class BackgroundStepService extends Service {
    public BackgroundStepService(Context applicationContext) {
        super();
    }

    public BackgroundStepService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.eyecuelab.survivalists.util.BackgroundStepReceiver");
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
