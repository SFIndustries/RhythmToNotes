package com.example.luka.sequencer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends Activity {

    Button buttonSelectMode, buttonCalibration, buttonChangeLanguage, buttonCredits;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

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
            }
        });

        buttonChangeLanguage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChooseLanguageActivity.class);
                startActivityForResult(intent, 1);
                //startActivity(intent);

            }
        });

        buttonCredits.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreditsActivity.class);
                startActivity(intent);

            }
        });

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


}
