package com.itbd.livewallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar process;
    private Timer timer;
    private int i = 0;
    private TextView countProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        process = findViewById(R.id.progressbar);
        countProgress = findViewById(R.id.countProgress);

        process.setProgress(0);

        final long period = 100;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //this repeats every 100 ms
                if (i < 100) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            countProgress.setText("Loading  "+i + "%");
                        }
                    });
                    process.setProgress(i);
                    i = i + 2;
                } else {
                    //closing the timer
                    timer.cancel();
                    // intent
                    startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                    finish();
                }
            }
        }, 0, period);
    }
}
