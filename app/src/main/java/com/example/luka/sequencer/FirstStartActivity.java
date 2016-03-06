package com.example.luka.sequencer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class FirstStartActivity extends Activity {

    Button buttonCalibration1;

    boolean firstStart;
    TextView textViewCalibration1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        final SharedPreferences sharedPref = FirstStartActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        firstStart = sharedPref.getBoolean("firstStart", true);

        //firstStart = true; //<---

        if (!firstStart)
        {
            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        else
        {
//            sharedPref = FirstStartActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putBoolean("firstStart", false);
//            editor.apply();
        }

        buttonCalibration1 = (Button) findViewById(R.id.buttonCalibration1);
        textViewCalibration1 = (TextView) findViewById(R.id.textViewCalibration1);

        buttonCalibration1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstStart", false);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), CalibrationActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = new Intent(this, ChooseLanguageActivity.class);
        startActivityForResult(intent, 1);

        //startActivity(intent);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case 1:
                String l=data.getStringExtra("l");
                if (l.equals("hr"))
                    textViewCalibration1.setText(R.string.calibrationDescription_hr);
                else
                    textViewCalibration1.setText(R.string.calibrationDescription_en);
                break;
        }
    }


}
