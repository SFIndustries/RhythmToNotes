package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainMenuActivity extends Activity {

    Button buttonSelectMode, buttonCalibration, buttonChangeLanguage, buttonCredits;
    boolean pressedOnce = false;

    // RADI

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        buttonSelectMode = (Button) findViewById(R.id.buttonSelectMode);
        buttonCalibration = (Button) findViewById(R.id.buttonCalibration);
        buttonChangeLanguage = (Button) findViewById(R.id.buttonChangeLanguage);
        buttonCredits = (Button) findViewById(R.id.buttonCredits);

        buttonSelectMode.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ModeSelectionActivity.class);
                startActivity(intent);
            }
        });

        buttonCalibration.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalibrationActivity.class);
                startActivity(intent);
                //finish();
                //return;
            }
        });

        buttonChangeLanguage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChooseLanguageActivity.class);
                startActivityForResult(intent, 1);
                //startActivity(intent);

            }
        });

        buttonCredits.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreditsActivity.class);
                startActivity(intent);

            }
        });

        SharedPreferences sharedPref = MainMenuActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case 1:
                recreate();
                break;

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        pressedOnce = false;
    }

    @Override
    public void onBackPressed()
    {
        if (!pressedOnce)
        {
            Toast.makeText(getApplicationContext(), (String) getResources().getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
            pressedOnce = true;
        }
        else
        {
            finish();
            return;
        }

    }


}
