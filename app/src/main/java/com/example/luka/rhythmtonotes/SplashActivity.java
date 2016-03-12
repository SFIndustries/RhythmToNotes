package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_DISPLAY_LENGTH = 2000;

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, FirstStartActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

    }


}
