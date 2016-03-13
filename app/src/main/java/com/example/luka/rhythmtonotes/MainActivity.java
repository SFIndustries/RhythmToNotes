package com.example.luka.rhythmtonotes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.os.Looper;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.SoundPool.OnLoadCompleteListener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class MainActivity extends Activity {

    int bpm = 120, bpmSleep,
            upper = 4, lower = 4, counter, max;

    int metronomeDelayCorrection = 130; // [ms]

    float _32note, _16note, _8note, _4note, _2note, _1note;
    float dot_16note, dot_8note, dot_4note, dot_2note;
    float triplet_1note, triplet_16note, triplet_8note, triplet_4note, triplet_2note;
    int shortestNote = 1;
    List<Integer> triplets = Arrays.asList(2, 5, 8, 11);
    List<Integer> nonTriplets = Arrays.asList(0, 1, 3, 4, 6, 9/*, 12*/);

    public volatile boolean play;
    boolean bpmPMdown = false;

    char bpmPM, mode = 'd'; // d = dotted, t = triplets, b = both (dotted+triplets)

    public TextView textViewBpm, textViewUpper, textViewLower,
            textViewFont, textViewMode;

    Button buttonBpmM, buttonBpmP, buttonStart, buttonStop,
            buttonUpperM, buttonUpperP, buttonLowerM, buttonLowerP,
            buttonClick, buttonModeM, buttonModeP,
            buttonGo;

    HorizontalScrollView horizontalScrollViewRhythm;
    LinearLayout linearLayoutRhythm;

    ImageView imageViewNote;

    public SoundPool soundPool;
    public int soundIDLow, soundIDHigh, soundIDStick;
    boolean /*plays = false,*/ loaded = false;
    float actVolume, maxVolume, volume;
    AudioManager audioManager;

    MetronomeThreadSet metronomeThreadSet;
    BpmChangeThread bpmChangeThread;
    MainActivity mainActivity;

    Timer timer = new Timer();
    long bpmPMdelay = 500; //[ms]

    public volatile List metronomeTimeList = new ArrayList();
    public volatile List metronomeErrorList;
    List ClickTimeList = new ArrayList();
    List<Float> notesList;
    List durationsList;

    String stringMetronomeTimeList = "";
    String rhythm;
    int height;
    String note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome_setings);

        AdView mAdView = (AdView) findViewById(R.id.adViewMetronomeSettings);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        textViewBpm = (TextView) findViewById(R.id.textViewTempo);
        textViewUpper = (TextView) findViewById(R.id.textViewUpper);
        textViewLower = (TextView) findViewById(R.id.textViewLower);
        //textViewFont = (TextView) findViewById(R.id.textViewFont);
        //textViewMode = (TextView) findViewById(R.id.textViewMode);

        textViewBpm.setText(Integer.toString(bpm));
        textViewUpper.setText(Integer.toString(upper));
        textViewLower.setText(Integer.toString(lower));
        //textViewFont.setText("ry");
        //textViewMode.setText("e.");

        Typeface font = Typeface.createFromAsset(getAssets(), "rhythms.ttf");
        //textViewFont.setTypeface(font);
        //textViewMode.setTypeface(font);

        bpmSleep = (int)((float)(60.0 / bpm) * 1000);

        buttonBpmM = (Button) findViewById(R.id.buttonTempoM);
        buttonBpmP = (Button) findViewById(R.id.buttonTempoP);
        buttonUpperM = (Button) findViewById(R.id.buttonUpperM);
        buttonUpperP = (Button) findViewById(R.id.buttonUpperP);
        buttonLowerM = (Button) findViewById(R.id.buttonLowerM);
        buttonLowerP = (Button) findViewById(R.id.buttonLowerP);
        //buttonModeM = (Button) findViewById(R.id.buttonModeM);
        //buttonModeP = (Button) findViewById(R.id.buttonModeP);
        buttonGo = (Button) findViewById(R.id.buttonGo);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        //buttonClick = (Button) findViewById(R.id.buttonClick);

        //horizontalScrollViewRhythm = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewRhythm);
        //linearLayoutRhythm = (LinearLayout) findViewById(R.id.linearLayoutRhythm);

        //height = horizontalScrollViewRhythm.getHeight();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //volume = actVolume / maxVolume;
        volume = maxVolume;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIDLow = soundPool.load(this, R.raw.clicklow1, 1);
        soundIDHigh = soundPool.load(this, R.raw.clickhigh1, 1);
        soundIDStick = soundPool.load(this, R.raw.stick, 1);

        View.OnTouchListener bpmPMListener = new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (bpmChangeThread != null) return false;
                    bpmPMdown = true;
                    if (v == buttonBpmM)
                    {
                        bpm--;
                        bpmSleep = (int) ((float) (60.0 / mainActivity.bpm) * 1000 * (4.0 / mainActivity.lower));
                        textViewBpm.setText(Integer.toString(mainActivity.bpm));
                        bpmPM = 'M';
                    }
                    else
                    {
                        bpm++;
                        bpmSleep = (int) ((float) (60.0 / mainActivity.bpm) * 1000 * (4.0 / mainActivity.lower));
                        textViewBpm.setText(Integer.toString(mainActivity.bpm));
                        bpmPM = 'P';
                    }


                    bpmChangeThread = new BpmChangeThread( mainActivity, bpmPM );
                    bpmChangeThread.start();

                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP)
                {

                    if (bpmChangeThread == null) return false;
                    bpmPMdown = false;

                    try
                    {
                        bpmChangeThread.join();
                    }catch(InterruptedException e) {}

                    bpmChangeThread = null;
                    return true;
                }

                return false;
            }

        };


        buttonBpmM.setOnTouchListener(bpmPMListener);

        buttonBpmP.setOnTouchListener(bpmPMListener);

/*        buttonClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickTimeList.add(System.nanoTime());
                soundPool.play(soundIDStick, 1, 1, 1, 0, 1f);

            }
        });*/



        /*buttonBpmP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bpm++;
                bpmSleep = (int)((float)(60.0 / bpm) * 1000 * (4.0/lower));
                textViewBpm.setText(Integer.toString(bpm));
            }
        });*/


        buttonLowerM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lower > 1) lower/=2;
                else return;
                bpmSleep = (int) ( (float) (60.0 / bpm) * 1000 * (4.0/lower) );
                textViewLower.setText(Integer.toString(lower));
                if(play)
                {
                    StopMetronomeThread();
                    metronomeTimeList = new ArrayList();
                    metronomeErrorList = new ArrayList();
                    StartMetronomeThread();
                }
            }
        });

        buttonLowerP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lower < 32) lower*=2;
                else return;
                bpmSleep = (int) ( (float) (60.0 / bpm) * 1000 * (4.0/lower) );
                textViewLower.setText(Integer.toString(lower));
                if(play)
                {
                    StopMetronomeThread();
                    metronomeTimeList = new ArrayList();
                    metronomeErrorList = new ArrayList();
                    StartMetronomeThread();
                }
            }
        });

        buttonUpperM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (upper > 1) upper--;
                else return;
                textViewUpper.setText(Integer.toString(upper));
                if(play)
                {
                    StopMetronomeThread();
                    StartMetronomeThread();
                }
            }
        });

        buttonUpperP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (upper < 32) upper++;
                else return;
                textViewUpper.setText(Integer.toString(upper));
                if(play)
                {
                    StopMetronomeThread();
                    StartMetronomeThread();
                }
            }
        });


        mainActivity = this;

        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                metronomeTimeList = new ArrayList();
                metronomeErrorList = new ArrayList();
                ClickTimeList = new ArrayList();
                StartMetronomeThread();

                //linearLayoutRhythm.removeAllViews();

                //playSound();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                StopMetronomeThread();

                PrintMetronomeError();


                /*
                imageViewNote = new ImageView(mainActivity);
                imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
                imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(linearLayoutRhythm.getHeight(), linearLayoutRhythm.getHeight()));
                imageViewNote.setImageResource(R.drawable.c_common_time);
                linearLayoutRhythm.addView(imageViewNote);
                */

                //StartWrite();

            }
        });

        Bundle extras = getIntent().getExtras();


//        buttonModeM.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                switch (mode)
//                {
//                    case 't':
//                        mode = 'd';
//                        textViewMode.setText("e.");
//                        break;
//                    case 'b':
//                        mode = 't';
//                        textViewMode.setText("Oe");
//                        break;
//                }
//            }
//
//        });
//
//        buttonModeP.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                switch (mode)
//                {
//                    case 'd':
//                        mode = 't';
//                        textViewMode.setText("Oe");
//                        break;
//                    case 't':
//                        mode = 'b';
//                        textViewMode.setText("e.+Oe");
//                        break;
//                }
//            }
//
//        });

        buttonGo.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                StopMetronomeThread();

                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                intent.putExtra("bpm", bpm);
                intent.putExtra("upper", upper);
                intent.putExtra("lower", lower);
                intent.putExtra("mode", mode);
                intent.putExtra("bpmSleep", bpmSleep);
                startActivity(intent);
            }

        });

        //CalculateDurations();

        //float testDuration = 20;
        //Note testNote = Write4(testDuration, new ArrayList());

    }


    private void CalculateDurations()
    {
        float bpmSleepTemp = bpmSleep * lower/4.0f;

        _32note = (float) (bpmSleepTemp / 8.0); // [ms], bmpSleep = _4note kod 4/4
        _16note = (float)(bpmSleepTemp / 4.0);
        _8note = (float)(bpmSleepTemp / 2.0);
        _4note = (float) bpmSleepTemp;
        _2note = (float)(2.0 * bpmSleepTemp);
        _1note = (float)(4.0 * bpmSleepTemp);

        dot_16note = (float)(_16note * 3.0/2);
        dot_8note = (float)(_8note * 3.0/2);
        dot_4note = (float)(_4note * 3.0/2);
        dot_2note = (float)(_2note * 3.0/2);

        triplet_16note = (float)(_8note / 3.0);
        triplet_8note = (float)(_4note / 3.0);
        triplet_4note = (float)(_2note / 3.0);
        triplet_2note = (float)(_1note / 3.0);
        triplet_1note = (float)((2.0 * _1note) / 3.0);

        notesList = Arrays.asList
                (_32note, _16note, _8note, _4note, _2note, _1note,
                dot_16note, dot_8note, dot_4note, dot_2note,
                /*triplet_16note,*/ triplet_8note, triplet_4note, triplet_2note, triplet_1note);

        Collections.sort(notesList);



    }

    String WriteNoteSimple (float noteDuration, int shortestNoteTemp)
    {
        float duration = 0;
        String note = "";
        int i;

        while ( notesList.get(shortestNoteTemp) <= noteDuration - duration )
        {

            for (i = 0; i < notesList.size(); i++)
            {
                if (duration + notesList.get(i) > noteDuration)
                    break;
            }

            duration += notesList.get(i - 1);
            note += i - 1 + ",";
        }

        return note;
    }


    int NumberOfTriplets(String notation)
    {
        if (notation.equals("")) return 0;
        //List<Integer> triplets = Arrays.asList(2, 5, 8, 11);

        List<String> temp = Arrays.asList(notation.split(","));
        int n = 0;
        for(int i=0; i<temp.size(); i++)
        {
            if (triplets.contains( Integer.parseInt(temp.get(i)) ))
                n++;
        }

        return n;
    }

    Note Write4 ( float duration, ArrayList usedNotes )
    {
        Note bestNote = new Note();
        bestNote.duration = 0;
        bestNote.notation = "";

        //if (duration < notesList.get(shortestNote)) return bestNote;

        // prva dulja nota iz liste mogucih
        for (int i=shortestNote; i<notesList.size(); i++)
        {
            if ( usedNotes.contains(i) || i == 3 ) continue;

            if (    mode == 'd' && triplets.contains(i)
                ||  mode == 't' && nonTriplets.contains(i))
                continue;

            //if ( triplets.contains(i) ) continue;
            //if ( nonTriplets.contains(i) ) continue;

            if ( notesList.get(i) > duration)
            {
                bestNote.duration = notesList.get(i);
                bestNote.notation = Integer.toString(i);
                break;
            }
        }

        // mislim da je ovo tu umjesto gore racunalo krivo (kad su triole valjda)
        //if (duration < notesList.get(shortestNote)) return bestNote;

        //mozda tu jel manje od pola?
        if (duration < notesList.get(shortestNote))
        {
            if ( duration < Math.abs(duration - bestNote.duration) )
            {
                bestNote.duration = 0;
                bestNote.notation = "";
                return bestNote;
            }
            else return bestNote;
        }



        // kombinacija kracih
        for (int i=shortestNote; i<notesList.size(); i++)
        {
            if ( notesList.get(i) > duration )
                break;

            if (    mode == 'd' && triplets.contains(i)
                    ||  mode == 't' && nonTriplets.contains(i))
                continue;

            //if ( triplets.contains(i) ) continue;
            //if ( nonTriplets.contains(i) ) continue;
                                                                        // 16 s tockom
            if ( usedNotes.contains(i) || notesList.get(i) > duration || i == 3 ) continue;
            ArrayList tempUsedNotes = new ArrayList(usedNotes);
            tempUsedNotes.add(i);

            Note tempNote = Write4(duration - notesList.get(i), tempUsedNotes );
            tempNote.duration += notesList.get(i);
            tempNote.notation = Integer.toString(i) + "," + tempNote.notation;

            if (    bestNote.duration == 0
                ||  Math.abs(duration - tempNote.duration /*- notesList.get(i)*/) < Math.abs(duration - bestNote.duration)

                ||  (( Math.abs( (/*notesList.get(i) + */tempNote.duration) - bestNote.duration) < /*1*/1)
                        // + prvo provjeri broj triola, a tek onda trajanje
                        &&  (       NumberOfTriplets(tempNote.notation) < NumberOfTriplets(bestNote.notation)
                            ||  (   NumberOfTriplets(tempNote.notation) == NumberOfTriplets(bestNote.notation)
                                    &&  (  Arrays.asList(tempNote.notation.split(",")).size() < Arrays.asList(bestNote.notation.split(",")).size() )
                                )
                            )
                    )

                )
            {
                bestNote.duration = tempNote.duration /*+ notesList.get(i)*/;
                bestNote.notation = /*Integer.toString(i) + "," + */tempNote.notation;
            }
        }

        return bestNote;
    }



//    String Write2 (long noteDuration)
//    {
//        float duration = 0, durationT = 0, durationT1 = 0;
//        int i = 0, numNotes = 0, numNotesT = 0;
//        String note = "", noteT = "";
//        String returnString;
//
//        while ( notesList.get(shortestNote) <= noteDuration - duration )
//        {
//
//            for (i = 0; i < notesList.size(); i++)
//            {
//                if (duration + notesList.get(i) > noteDuration)
//                    break;
//            }
//
//            duration += notesList.get(i - 1);
//            note += i - 1 + ",";
//
//
//        }
//
//        //nakon prvog zapisivanja jednostavno zamijeni najkracu notu onom koja najbolje pase
//
//
//
//        List<String> temp;
//        temp = (Arrays.asList(note.split(",")));
//
//
//        //durationT je trajanje alternativne kombinacije (prve vece ili manje)
//
//
//        // triole su 2, 5, 8, 11
//
//        // ako je najkraca nota (u kombinaciji) triola, umjesto nje u kombinaciju dodaj prvu notu koja traje dulje
//        // tj. notu s tockom
//        if ( temp.get(temp.size()-1).equals("2")) durationT = duration + (notesList.get(3) - notesList.get(2));
//        else if ( temp.get(temp.size()-1).equals("5")) durationT = duration + (notesList.get(6) - notesList.get(5));
//        else if ( temp.get(temp.size()-1).equals("8")) durationT = duration + (notesList.get(9) - notesList.get(8));
//        else if ( temp.get(temp.size()-1).equals("11")) durationT = duration + (notesList.get(12) - notesList.get(11));
//        if (durationT > 0)
//            noteT = WriteNoteSimple(durationT, shortestNote);
//
//        else
//        {
//            //while (notesList.get(shortestNote) <= duration + notesList.get(shortestNote) - durationT)
//            while (durationT <= duration) //isti uvjet kao ovaj zakomentirani
//            {
//
//                for (i = 0; i < notesList.size(); i++)
//                {
//                    if (durationT + notesList.get(i) > duration + notesList.get(shortestNote))
//                        break;
//                }
//
//                durationT += notesList.get(i - 1);
//                noteT += i - 1 + ",";
//
//            }
//
//
//
//
//            // ako je najkraca nota prva kraca ili prva dulja od triole, umjesto nje stavi triolu ako bolje pase
//
//            if ( temp.get(temp.size()-1).equals("1"))
//            {
//                durationT1 = duration + (notesList.get(2) - notesList.get(1));
//            }
//            else if ( temp.get(temp.size()-1).equals("3"))
//            {
//                durationT1 = duration + (notesList.get(2) - notesList.get(3));
//            }
//
//            else if ( temp.get(temp.size()-1).equals("4"))
//            {
//                durationT1 = duration + (notesList.get(5) - notesList.get(4));
//            }
//            else if ( temp.get(temp.size()-1).equals("6"))
//            {
//                durationT1 = duration + (notesList.get(5) - notesList.get(6));
//            }
//
//            else if ( temp.get(temp.size()-1).equals("7"))
//            {
//                durationT1 = duration + (notesList.get(8) - notesList.get(7));
//            }
//            else if ( temp.get(temp.size()-1).equals("9"))
//            {
//                durationT1 = duration + (notesList.get(8) - notesList.get(9));
//            }
//
//            else if ( temp.get(temp.size()-1).equals("10"))
//            {
//                durationT1 = duration + (notesList.get(11) - notesList.get(10));
//            }
//            else if ( temp.get(temp.size()-1).equals("12"))
//            {
//                durationT1 = duration + (notesList.get(11) - notesList.get(12));
//            }
//
//
//            if (Math.abs(noteDuration - durationT) > Math.abs(noteDuration - durationT1))
//            {
//                durationT = durationT1;
//                noteT = WriteNoteSimple(durationT, shortestNote);
//            }
//
//
//        }
//
//
//        //ako je durationT blize stvarnom trajanju od duration
//        if (Math.abs(noteDuration - duration) > Math.abs(noteDuration - durationT))
//        {
//            temp = (Arrays.asList(noteT.split(",")));
//            returnString = noteT;
//        }
//        else
//        {
//            temp = (Arrays.asList(note.split(",")));
//            returnString = note;
//        }
//
//        for (int j = 0; j < temp.size(); j++ )
//        {
//            imageViewNote = new ImageView(this);
//            imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(height/3, height));
//
//            switch (Integer.parseInt(temp.get(j))) {
//                case 0:
//                    imageViewNote.setImageResource (R.drawable._32nd_note);
//                    break;
//                /*case 1:
//                    imageViewNote.setImageResource (R.drawable.lower_16th_note);
//                    break;*/
//                case 1:
//                    imageViewNote.setImageResource(R.drawable._16th_note);
//                    break;
//                case 2:
//                    imageViewNote.setImageResource(R.drawable.lower_8th_note);
//                    break;
//                case 3:
//                    imageViewNote.setImageResource(R.drawable._16th_note_dotted);
//                    break;
//                case 4:
//                    imageViewNote.setImageResource(R.drawable._8th_note);
//                    break;
//                case 5:
//                    imageViewNote.setImageResource(R.drawable.lower_4th_note);
//                    break;
//                case 6:
//                    imageViewNote.setImageResource(R.drawable._8th_note_dotted);
//                    break;
//                case 7:
//                    imageViewNote.setImageResource(R.drawable._4th_note);
//                    break;
//                case 8:
//                    imageViewNote.setImageResource(R.drawable.lower_half_note);
//                    break;
//                case 9:
//                    imageViewNote.setImageResource(R.drawable._4th_note_dotted);
//                    break;
//                case 10:
//                    imageViewNote.setImageResource(R.drawable.half_note);
//                    break;
//                case 11:
//                    imageViewNote.setImageResource(R.drawable.whole_note_dotted);
//                    break;
//                case 12:
//                    imageViewNote.setImageResource(R.drawable.half_note_dotted);
//                    break;
//                case 13:
//                    imageViewNote.setImageResource (R.drawable.whole_note);
//                    break;
//            }
//
//            linearLayoutRhythm.addView(imageViewNote);
//
//        }
//
//        return returnString;
//
//
//        //inace je duration blize stvarnom trajanju od durationT
////        else
////        {
////            //List<String> temp;
////            temp = (Arrays.asList(note.split(",")));
////
////
////            for (int j = 0; j < temp.size(); j++ )
////            {
////                imageViewNote = new ImageView(this);
////                imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
////                imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(height/3, height));
////
////                switch (Integer.parseInt(temp.get(j))) {
////                    case 0:
////                        imageViewNote.setImageResource (R.drawable._32nd_note);
////                        break;
////                    /*case 1:
////                        imageViewNote.setImageResource (R.drawable.lower_16th_note);
////                        break;*/
////                    case 1:
////                        imageViewNote.setImageResource(R.drawable._16th_note);
////                        break;
////                    case 2:
////                        imageViewNote.setImageResource(R.drawable.lower_8th_note);
////                        break;
////                    case 3:
////                        imageViewNote.setImageResource(R.drawable._16th_note_dotted);
////                        break;
////                    case 4:
////                        imageViewNote.setImageResource(R.drawable._8th_note);
////                        break;
////                    case 5:
////                        imageViewNote.setImageResource(R.drawable.lower_4th_note);
////                        break;
////                    case 6:
////                        imageViewNote.setImageResource(R.drawable._8th_note_dotted);
////                        break;
////                    case 7:
////                        imageViewNote.setImageResource(R.drawable._4th_note);
////                        break;
////                    case 8:
////                        imageViewNote.setImageResource(R.drawable.lower_half_note);
////                        break;
////                    case 9:
////                        imageViewNote.setImageResource(R.drawable._4th_note_dotted);
////                        break;
////                    case 10:
////                        imageViewNote.setImageResource(R.drawable.half_note);
////                        break;
////                    case 11:
////                        imageViewNote.setImageResource(R.drawable.whole_note_dotted);
////                        break;
////                    case 12:
////                        imageViewNote.setImageResource(R.drawable.half_note_dotted);
////                        break;
////                    case 13:
////                        imageViewNote.setImageResource (R.drawable.whole_note);
////                        break;
////                }
////
////                linearLayoutRhythm.addView(imageViewNote);
////
////            }
////
////            return note;
////        }
//
//    }
//
//    String WriteRest (long noteDuration)
//    {
//        float duration = 0, durationT = 0;
//        int i, numNotes = 0, numNotesT = 0;
//        String note = "", noteT = "";
//
//
//        while ( notesList.get(shortestNote) <= noteDuration - duration )
//        {
//
//            for (i = 0; i < notesList.size(); i++)
//            {
//                if (duration + notesList.get(i) > noteDuration)
//                    break;
//            }
//
//            duration += notesList.get(i - 1);
//            note += i - 1 + ",";
//
//
//        }
//
//        List<String> temp;
//        temp = (Arrays.asList(note.split(",")));
//
//        if ( temp.get(temp.size()-1).equals("2")) durationT += duration + (notesList.get(3) - notesList.get(2));
//        else if ( temp.get(temp.size()-1).equals("5")) durationT += duration + (notesList.get(6) - notesList.get(5));
//        else
//        {
//            while (notesList.get(shortestNote) <= duration + notesList.get(shortestNote) - durationT) {
//
//                for (i = 0; i < notesList.size(); i++)
//                {
//                    if (durationT + notesList.get(i) > duration + notesList.get(shortestNote))
//                        break;
//                }
//
//                durationT += notesList.get(i - 1);
//                noteT += i - 1 + ",";
//
//            }
//        }
//        if (Math.abs(noteDuration - duration) > Math.abs(noteDuration - durationT))
//        {
//            //List<String> temp;
//            temp = (Arrays.asList(noteT.split(",")));
//
//            for (int j = 0; j < temp.size(); j++ )
//            {
//                imageViewNote = new ImageView(this);
//                imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
//                imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(height/3, height));
//
//                switch (Integer.parseInt(temp.get(j))) {
//                    case 0:
//                        imageViewNote.setImageResource (R.drawable._32nd_note);
//                        break;
//                    /*case 1:
//                        imageViewNote.setImageResource (R.drawable.lower_16th_note);
//                        break;*/
//                    case 1:
//                        imageViewNote.setImageResource(R.drawable._16th_note);
//                        break;
//                    case 2:
//                        imageViewNote.setImageResource(R.drawable.lower_8th_note);
//                        break;
//                    case 3:
//                        imageViewNote.setImageResource(R.drawable._16th_note_dotted);
//                        break;
//                    case 4:
//                        imageViewNote.setImageResource(R.drawable._8th_note);
//                        break;
//                    case 5:
//                        imageViewNote.setImageResource(R.drawable.lower_4th_note);
//                        break;
//                    case 6:
//                        imageViewNote.setImageResource(R.drawable._8th_note_dotted);
//                        break;
//                    case 7:
//                        imageViewNote.setImageResource(R.drawable._4th_note);
//                        break;
//                    case 8:
//                        imageViewNote.setImageResource(R.drawable.lower_half_note);
//                        break;
//                    case 9:
//                        imageViewNote.setImageResource(R.drawable._4th_note_dotted);
//                        break;
//                    case 10:
//                        imageViewNote.setImageResource(R.drawable.half_note);
//                        break;
//                    case 11:
//                        imageViewNote.setImageResource(R.drawable.whole_note_dotted);
//                        break;
//                    case 12:
//                        imageViewNote.setImageResource(R.drawable.half_note_dotted);
//                        break;
//                    case 13:
//                        imageViewNote.setImageResource (R.drawable.whole_note);
//                        break;
//                }
//
//                linearLayoutRhythm.addView(imageViewNote);
//
//            }
//
//            return noteT;
//
//        }
//
//        else
//        {
//            //List<String> temp;
//            temp = (Arrays.asList(note.split(",")));
//
//
//            for (int j = 0; j < temp.size(); j++ )
//            {
//                imageViewNote = new ImageView(this);
//                imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
//                imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(height/3, height));
//
//                switch (Integer.parseInt(temp.get(j))) {
//                    case 0:
//                        imageViewNote.setImageResource (R.drawable._32nd_note);
//                        break;
//                    /*case 1:
//                        imageViewNote.setImageResource (R.drawable.lower_16th_note);
//                        break;*/
//                    case 1:
//                        imageViewNote.setImageResource(R.drawable._16th_note);
//                        break;
//                    case 2:
//                        imageViewNote.setImageResource(R.drawable.lower_8th_note);
//                        break;
//                    case 3:
//                        imageViewNote.setImageResource(R.drawable._16th_note_dotted);
//                        break;
//                    case 4:
//                        imageViewNote.setImageResource(R.drawable._8th_note);
//                        break;
//                    case 5:
//                        imageViewNote.setImageResource(R.drawable.lower_4th_note);
//                        break;
//                    case 6:
//                        imageViewNote.setImageResource(R.drawable._8th_note_dotted);
//                        break;
//                    case 7:
//                        imageViewNote.setImageResource(R.drawable._4th_note);
//                        break;
//                    case 8:
//                        imageViewNote.setImageResource(R.drawable.lower_half_note);
//                        break;
//                    case 9:
//                        imageViewNote.setImageResource(R.drawable._4th_note_dotted);
//                        break;
//                    case 10:
//                        imageViewNote.setImageResource(R.drawable.half_note);
//                        break;
//                    case 11:
//                        imageViewNote.setImageResource(R.drawable.whole_note_dotted);
//                        break;
//                    case 12:
//                        imageViewNote.setImageResource(R.drawable.half_note_dotted);
//                        break;
//                    case 13:
//                        imageViewNote.setImageResource (R.drawable.whole_note);
//                        break;
//                }
//
//                linearLayoutRhythm.addView(imageViewNote);
//
//            }
//
//            return note;
//        }
//
//    }


    private void PrintMetronomeError()

    {
        if ( metronomeTimeList.size() < 2 ) return;
        metronomeErrorList = new ArrayList();

        long delta = 0;

        for(int i=0; i < metronomeTimeList.size()-1; i++)
        {
            Object t0 = metronomeTimeList.get(i);
            Object t1 = metronomeTimeList.get(i+1);
            delta = (long)(t1) - (long)(t0);
            metronomeErrorList.add(Math.abs(delta - bpmSleep*1000000));
            stringMetronomeTimeList += delta + ", ";
        }

        Collections.sort(metronomeErrorList);
        Collections.reverse(metronomeErrorList);
        delta =  (long) metronomeErrorList.get(0);
        String deltaString = (Long.toString(delta / 1000000 - metronomeDelayCorrection) + "." + String.format("%06d", (delta % 1000000)));

        //Toast.makeText(getApplicationContext(), (String) stringMetronomeTimeList, Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), (String) (deltaString + " [ms]"), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (metronomeThreadSet == null) return;
        play = false;
        metronomeThreadSet.endLooper();
        try
        {
            metronomeThreadSet.join();
        }catch(InterruptedException e) {}

        metronomeThreadSet = null;

        if (bpmChangeThread == null) return;
        bpmChangeThread.endLooper();
        try
        {
            bpmChangeThread.join();
        }catch(InterruptedException e) {}

        bpmChangeThread = null;
    }

    void StartMetronomeThread()
    {
        if (metronomeThreadSet != null) return;
        metronomeThreadSet = new MetronomeThreadSet(mainActivity);
        metronomeThreadSet.start();
    }

    void StopMetronomeThread()
    {
        if (metronomeThreadSet == null) return;
        play = false;

        try
        {
            metronomeThreadSet.join();
        }catch(InterruptedException e) {}


        metronomeThreadSet = null;
    }


    public void playClickHigh()
    {

        soundPool.play(soundIDHigh, 1, 1, 1, 0, 1f);

        //Toast.makeText(this, "Played sound", Toast.LENGTH_SHORT).show()

    }

    public void playClickLow()
    {

        soundPool.play(soundIDLow, 1, 1, 1, 0, 1f);

        //Toast.makeText(this, "Played sound", Toast.LENGTH_SHORT).show()

    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}

class MetronomeThreadSet extends Thread {

    MainActivity mainActivity;

    private volatile Looper looper;
    public int counter;

    public MetronomeThreadSet(MainActivity pmainActivity)
    {
        mainActivity = pmainActivity;
    }

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
        int counter = mainActivity.upper;
        mainActivity.play = true;

        long start, start1, delay, end;

        while (mainActivity.play)
        {
            //mainActivity.playSound();

            start = System.nanoTime();

            if (counter == mainActivity.upper)
            {
                mainActivity.metronomeTimeList.add(System.nanoTime() + 1000000 * mainActivity.metronomeDelayCorrection);
                mainActivity.soundPool.play(mainActivity.soundIDLow, 1, 1, 1, 0, 1f);


                //mainActivity.playClickLow();
                //mainActivity.playClickHigh();

                counter = 1;
            }
            else
            {
                mainActivity.metronomeTimeList.add(System.nanoTime());
                mainActivity.soundPool.play(mainActivity.soundIDHigh, 1, 1, 1, 0, 1f);


                //mainActivity.playClickHigh();


                counter++;
            }

            delay = System.nanoTime() - start;
            start1 = System.nanoTime();
            end = start1 + mainActivity.bpmSleep * 1000000 - (delay);
            while ( System.nanoTime() < end );

            /*
            try{
                Thread.sleep(mainActivity.bpmSleep,0);
            }catch(InterruptedException e){}
            */
        }

        endLooper();

    }
}

class BpmChangeThread extends Thread
{
    MainActivity mainActivity;

    private volatile Looper looper;
    public int counter, max;
    long startSleep = 250;
    char bpmPM;

    public BpmChangeThread(MainActivity pmainActivity, char pbpmPM)
    {
        mainActivity = pmainActivity;
        bpmPM = pbpmPM;
    }

    public void run()
    {
        Looper.prepare();
        looper = Looper.myLooper();

        ChangeBpm();

        Looper.loop();
    }

    void ChangeBpm()
    {
        //mainActivity.bpmPMdown = true;

        while ( mainActivity.bpmPMdown )
        {
            try {
                if ( mainActivity.bpmPMdown )
                {
                    Thread.sleep(startSleep,0);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (!mainActivity.bpmPMdown) break;

            if (startSleep > 50) startSleep -= 50;

            if (bpmPM == 'M')
            {
                if (mainActivity.bpm > 1) mainActivity.bpm--;
            }
            else
            {
                if (mainActivity.bpm < 400) mainActivity.bpm++;
            }

            mainActivity.bpmSleep = (int) ((float) (60.0 / mainActivity.bpm) * 1000 * (4.0 / mainActivity.lower));
            mainActivity.textViewBpm.post(new Runnable() {
                public void run() {
                    mainActivity.textViewBpm.setText(Integer.toString(mainActivity.bpm));
                }
            });



        }

        endLooper();

    }

    public void endLooper(){
        looper.quit();
    }

}

class Note
{
    public String notation;
    public float duration;
}

class IntObj
{
    public int value;
}