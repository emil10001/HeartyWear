package io.hearty.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import io.hearty.witness.Reporter;
import io.hearty.witness.Witness;

public class MainActivity extends Activity implements Reporter {
    private TextView tv;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, WearStepService.class));
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.steps);
        tv.setText(String.valueOf(StepsTaken.getSteps()));
    }

    @Override
    public void onResume() {
        Witness.register(StepsTaken.class, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        Witness.remove(StepsTaken.class, this);
        super.onPause();
    }

    @Override
    public void notifyEvent(final Object o) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (o instanceof StepsTaken)
                    tv.setText(String.valueOf(StepsTaken.getSteps()));
            }
        });

    }
}
