package com.eyecuelab.survivalists;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by eyecuelab on 5/6/16.
 */
public class DailyCountResetService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DailyCountResetService(String name) {
        super(name);
    }
    public DailyCountResetService() {
        super("DailyCountResetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Integer totalSteps = intent.getIntExtra("dailyTotalSteps", 0);

        Intent resetIntent = new Intent (getApplicationContext(), MainActivity.class);
        resetIntent.putExtra("resetDailySteps", 0);
        resetIntent.putExtra("resetRecordedStepsCount", totalSteps);

    }
}
