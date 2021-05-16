package com.cast.tv.screen.mirroring.iptv.ui.screencast.errorscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.ui.screencast.selectscreen.SelectScreenActivity;

import java.util.Timer;
import java.util.TimerTask;

public class ErrorScreenActivity extends AppCompatActivity {

    private TextView mBackArea;
    private LinearLayout mBackButton;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_screen);

        mBackArea = findViewById(R.id.back_button);
        mBackButton = findViewById(R.id.back_to_player_button);

        mBackButton.setOnClickListener(view -> returnToSelectScreenActivity());

        startCountDown(7);
    }

    @Override
    public void onBackPressed() {
        returnToSelectScreenActivity();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) mTimer.cancel();

        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void startCountDown(int timeToClose) {
        mBackArea.setText(getText(R.string.activity_error_screen_back_text) + " " + timeToClose + "s");

        if (timeToClose <= 0) {
            returnToSelectScreenActivity();
            return;
        }

        if (mTimer != null) mTimer.cancel();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    startCountDown(timeToClose - 1);
                });
            }
        }, 1000);
    }

    private void returnToSelectScreenActivity() {
        Intent intent = new Intent(ErrorScreenActivity.this, SelectScreenActivity.class);
        startActivity(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}
