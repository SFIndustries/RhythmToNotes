package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Looper;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class CalibrationActivity extends Activity
{
    Button buttonContinue, buttonRepeat;
    TextView textViewCalibration, textViewCountdown;
    RelativeLayout relativeLayout, relativeLayoutCountdown;

    boolean loaded = false, countdownStarted = false, firstStart = true;
    float actVolume, maxVolume, volume;
    long soundTime, clickTime, delay;
    public SoundPool soundPool;
    public int soundIDLow, soundIDHigh, soundIDStick;
    AudioManager audioManager;

    CountdownThread countdownThread;
    CalibrationActivity calibrationActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //test

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

//        AdView mAdView = (AdView) findViewById(R.id.adViewCalibration);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        buttonContinue = (Button) findViewById(R.id.buttonContinue);
        buttonRepeat = (Button) findViewById(R.id.buttonRepeat);
        textViewCountdown = (TextView) findViewById(R.id.textViewCountdown);
        relativeLayoutCountdown = (RelativeLayout) findViewById(R.id.relativeLayoutCountdown);

//        Locale locale = new Locale("hr");
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        this.getApplicationContext().getResources().updateConfiguration(config, null);

        calibrationActivity = this;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //volume = actVolume / maxVolume;
        volume = maxVolume;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIDLow = soundPool.load(this, R.raw.clicklow3, 1);
        soundIDHigh = soundPool.load(this, R.raw.clickhigh3, 1);

        //countdownThread = new CountdownThread(this);



        View.OnTouchListener calibrationListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    clickTime = System.nanoTime();
                    if (countdownThread == null) return true;


                    try
                    {
                        countdownThread.join();
                    }catch(InterruptedException e) {}

                    countdownThread = null;

                    delay = Math.max(0, clickTime - soundTime);
                    String deltaString = (Long.toString(delay / 1000000) + "." + String.format("%06d", Math.abs(delay % 1000000)));
                    Toast.makeText(getApplicationContext(), (String) ( getResources().getString(R.string.latency) + " = " +  deltaString + " [ms]"), Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPref = CalibrationActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("metronomeDelayCorrection", Math.max(0, (int) delay / 1000000));
                    editor.apply();

                    buttonContinue.setVisibility(View.VISIBLE);
                    buttonRepeat.setVisibility(View.VISIBLE);
                }
                else
                {
                    //relativeLayoutCountdown.setBackgroundColor(Color.WHITE);
                }

                return true;
            }
        };

        relativeLayoutCountdown.setOnTouchListener(calibrationListener);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final SharedPreferences sharedPref = CalibrationActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
                boolean firstStartSP = sharedPref.getBoolean("firstStart", true);

                if ( firstStartSP )
                {
                    Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intent);
                }
                else
                {
                    finish();
                    return;
                }


            }
        });

        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonContinue.setVisibility(View.GONE);
                buttonRepeat.setVisibility(View.GONE);

                StartCountdown();
            }
        });

        StartCountdown();

    }

    protected void OnResume()
    {
        super.onResume();
    }

    void StartCountdown()
    {
        countdownThread = new CountdownThread(calibrationActivity);
        countdownThread.start();
    }


}

class CountdownThread extends Thread
{

    private volatile Looper looper;
    CalibrationActivity calibrationActivity;
    int sleep = 500; // [ms]

    public CountdownThread(CalibrationActivity calibrationActivityP)
    {
        calibrationActivity = calibrationActivityP;
    };

    public void run()
    {
        Looper.prepare();
        looper = Looper.myLooper();

        StartMetronome();

        Looper.loop();
    }

    public void endLooper(){
        looper.quit();
    }

    public void StartMetronome()
    {

        long start, start1, delay, end;

        if (calibrationActivity.firstStart)
        {
            try{
                Thread.sleep(500,0);
            }catch(InterruptedException e){}

            calibrationActivity.firstStart = false;
        }

//        calibrationActivity.textViewCountdown.post(new Runnable() {
//            public void run() {
//                calibrationActivity.textViewCountdown.setText("4");
//            }
//        });

        for (int i = 0; i<5; i++) {
            //mainActivity.playSound();

            start = System.nanoTime();

            if (i == 0 || i == 4)
            {
                calibrationActivity.soundPool.play(calibrationActivity.soundIDLow, 1, 1, 1, 0, 1f);


                if (i == 4)
                    calibrationActivity.soundTime = System.nanoTime();

            } else
            {
                calibrationActivity.soundPool.play(calibrationActivity.soundIDHigh, 1, 1, 1, 0, 1f);

            }

            final int iTemp = i;
//            calibrationActivity.textViewCountdown.post(new Runnable() {
//                public void run() {
//                    calibrationActivity.textViewCountdown.setText(Integer.toString(4-iTemp));
//                }
//            });

            delay = System.nanoTime() - start;
            start1 = System.nanoTime();
            end = start1 + sleep * 1000000 - (delay);
            while (System.nanoTime() < end) ;

            /*
            try{
                Thread.sleep(mainActivity.bpmSleep,0);
            }catch(InterruptedException e){}
            */
        }

        endLooper();

    }
}