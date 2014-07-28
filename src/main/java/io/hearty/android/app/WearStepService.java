package io.hearty.android.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import io.hearty.witness.Witness;

/**
 * Created by ejf3 on 7/26/14.
 */
public class WearStepService extends Service implements SensorEventListener {
    public static final String TAG = "WearStepService";
    private static final long THREE_MINUTES = 3 * 60 * 1000;
    private static final String STEP_COUNT_PATH = "/step-count";
    private static final String STEP_COUNT_KEY = "step-count";

    private SensorManager sensorManager;
    private Sensor countSensor;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        setAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        getSensorManager();
        getCountSensor();
        getGoogleClient();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getGoogleClient() {
        if (null != mGoogleApiClient)
            return;

        Log.d(TAG, "getGoogleClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * if the countSensor is null, try initializing it, and try registering it with sensorManager
     */
    private void getCountSensor() {
        if (null != countSensor)
            return;

        Log.d(TAG, "getCountSensor");
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        registerCountSensor();
    }

    /**
     * if the countSensor exists, then try registering
     */
    private void registerCountSensor() {
        if (countSensor == null)
            return;

        Log.d(TAG, "sensorManager.registerListener");
        sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * if the sensorManager is null, initialize it, and try registering the countSensor
     */
    private void getSensorManager() {
        if (null != sensorManager)
            return;

        Log.d(TAG, "getSensorManager");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerCountSensor();
    }

    private void setAlarm() {
        Log.d(TAG, "setAlarm");

        Intent intent = new Intent(this, AlarmNotify.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long firstRun = System.currentTimeMillis() + THREE_MINUTES;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstRun, THREE_MINUTES, pendingIntent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
            StepsTaken.updateSteps(event.values.length);

        sendToPhone();
        updateNotification();
        Witness.notify(new StepsTaken());
    }

    private void sendToPhone() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(STEP_COUNT_PATH);
        putDataMapRequest.getDataMap().putInt(STEP_COUNT_KEY, StepsTaken.getSteps());
        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        if (!mGoogleApiClient.isConnected())
            return;

        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "attempt to put data was a " +
                        (dataItemResult.getStatus().isSuccess()? "success!"
                                : "failure =( " + dataItemResult.getStatus().getStatusCode()));
            }
        });
    }

    private void updateNotification() {
        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher);

        builder.setContentText("steps: " + StepsTaken.getSteps());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // drop these messages
    }
}
