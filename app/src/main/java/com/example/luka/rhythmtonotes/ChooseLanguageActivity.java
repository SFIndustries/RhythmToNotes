package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Locale;

public class ChooseLanguageActivity extends Activity
{
    //test1

    ImageView imageViewHr, imageViewEn;
    String language = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

        imageViewHr = (ImageView) findViewById(R.id.imageViewHr);
        imageViewEn = (ImageView) findViewById(R.id.imageViewEn);

        imageViewHr.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                language = "hr";
                SetLocale(language);

                //Intent resultIntent = getIntent();
                //setResult(Activity.RESULT_OK, resultIntent);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("l", "hr");
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
                return;
            }
        });

        imageViewEn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                language = "en";
                SetLocale(language);

                //Intent resultIntent = new Intent();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("l", "en");
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
                return;
            }
        });

    }

    void SetLocale(String language)
    {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getApplicationContext().getResources().updateConfiguration(config, null);
    }


}
