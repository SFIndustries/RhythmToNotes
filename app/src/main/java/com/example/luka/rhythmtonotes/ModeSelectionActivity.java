package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ModeSelectionActivity extends Activity {

    Button buttonModeDotted, buttonModeTriplets, buttonModeDottedPTriplets, buttonStartMode;
    TextView textViewChooseMode, textViewModeDescription;
    ImageView imageView;

    char chosenMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        buttonModeDotted = (Button) findViewById(R.id.buttonModeDotted);
        buttonModeTriplets = (Button) findViewById(R.id.buttonModeTriplets);
        buttonModeDottedPTriplets = (Button) findViewById(R.id.buttonModeDottedPTriplets);
        buttonStartMode = (Button) findViewById(R.id.buttonStartMode);

        textViewChooseMode = (TextView) findViewById(R.id.textViewChooseMode);
        textViewModeDescription = (TextView) findViewById(R.id.textViewModeDescription);

        imageView = (ImageView) findViewById(R.id.imageView);

        HideDescriptions();

        buttonModeDotted.setOnClickListener
                (
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        ShowDescriptions();
                        chosenMode = 'd';
                        textViewModeDescription.setText(R.string.dotted_description);
                        imageView.setImageResource(R.drawable.dottedrhythm);
                    }
                }
                );

        buttonModeTriplets.setOnClickListener
                (
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        ShowDescriptions();
                        chosenMode = 't';
                        textViewModeDescription.setText(R.string.triplets_description);
                        imageView.setImageResource(R.drawable.tripletsrhythm);

                    }
                }
                );

        buttonModeDottedPTriplets.setOnClickListener
                (
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        ShowDescriptions();
                        chosenMode = 'b';
                        textViewModeDescription.setText(R.string.dotted_plus_triplets_description);
                        imageView.setImageResource(R.drawable.dottedplustripletsrhythm);
                    }
                }
                );

        buttonStartMode.setOnClickListener
                (
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                        intent.putExtra("mode", chosenMode);

                        SharedPreferences sharedPref = ModeSelectionActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("mode", Character.toString(chosenMode));
                        editor.apply();

                        startActivity(intent);
                    }
                }
                );
    }

    void HideDescriptions()
    {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        p.weight = 1;

        textViewChooseMode.setLayoutParams(p);
    }

    void ShowDescriptions()
    {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        p.weight = 0;

        textViewChooseMode.setLayoutParams(p);
    }

}
