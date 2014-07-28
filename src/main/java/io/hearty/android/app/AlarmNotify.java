package io.hearty.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ejf3 on 7/17/14.
 */
public class AlarmNotify extends BroadcastReceiver {
    private static final String TAG = "AlarmNotify";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "alarm fired");
        context.startService(new Intent(context, WearStepService.class));
    }
}
