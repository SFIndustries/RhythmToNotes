package com.example.luka.sequencer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import android.print.pdf.PrintedPdfDocument;
import android.print.PrintAttributes;
import android.print.PrintAttributes.Builder;
import android.graphics.pdf.PdfDocument;
import java.io.File;
import android.os.Environment;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteActivity extends Activity
{
    int bpm = 120, bpmSleep,
            upper = 4, lower = 4, counter, max;

    int metronomeDelayCorrection = 130; // [ms]
    int metronomeDelayCorrectionDefault = 130;

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
            textViewNotation, textViewMode;

    Button buttonBpmM, buttonBpmP, buttonStart1, buttonStop1,
            buttonUpperM, buttonUpperP, buttonLowerM, buttonLowerP,
            buttonClick1, buttonModeM, buttonModeP,
            buttonGo, buttonPDF;

    HorizontalScrollView horizontalScrollViewRhythm;

    ImageView imageViewNote;

    public SoundPool soundPool;
    public int soundIDLow, soundIDHigh, soundIDStick;
    boolean /*plays = false,*/ loaded = false;
    float actVolume, maxVolume, volume;
    AudioManager audioManager;

    MetronomeThreadWrite metronomeThreadWrite;
    BpmChangeThread bpmChangeThread;
    WriteActivity writeActivity;

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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        buttonClick1 = (Button) findViewById(R.id.buttonClick1);
        buttonStart1 = (Button) findViewById(R.id.buttonStart1);
        //buttonStop1 = (Button) findViewById(R.id.buttonStop1);
        //buttonPDF = (Button) findViewById(R.id.buttonPDF);
        textViewNotation = (TextView) findViewById(R.id.textViewNotation);

//        Typeface font = Typeface.createFromAsset(getAssets(), "rhythms.ttf");
//        textViewNotation.setTypeface(font);
//        textViewNotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50.0f);

        textViewNotation.setText(R.string.write_description);

        Bundle extras = getIntent().getExtras();
        mode = extras.getChar("mode");
        bpm = extras.getInt("bpm");
        bpmSleep = extras.getInt("bpmSleep");
        upper = extras.getInt("upper");
        lower = extras.getInt("lower");

        SharedPreferences sharedPref = WriteActivity.this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);

        String tempMode = (sharedPref.getString("mode", "d"));
        mode = tempMode.charAt(0);

        metronomeDelayCorrection = sharedPref.getInt("metronomeDelayCorrection", metronomeDelayCorrectionDefault);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //volume = actVolume / maxVolume;
        volume = maxVolume;

        writeActivity = this;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIDLow = soundPool.load(this, R.raw.clicklow1, 1);
        soundIDHigh = soundPool.load(this, R.raw.clickhigh1, 1);
        soundIDStick = soundPool.load(this, R.raw.stick, 1);

        buttonClick1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //soundPool.play(soundIDStick, 1, 1, 1, 0, 1f);
                    ClickTimeList.add(System.nanoTime());

                }
                return false;
            }
        });

        buttonStart1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!play)
                {
                    Typeface font = Typeface.createFromAsset(getAssets(), "rhythms.ttf");
                    textViewNotation.setTypeface(font);
                    textViewNotation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50.0f);
                    textViewNotation.setGravity(Gravity.CENTER_VERTICAL);

                    metronomeTimeList = new ArrayList();
                    metronomeErrorList = new ArrayList();
                    ClickTimeList = new ArrayList();
                    StartMetronomeThread();

                    textViewNotation.setText("");

                    buttonStart1.setText("Stop");
                }
                else
                {
                    StopMetronomeThread();

                    StartWrite();
                    buttonStart1.setText("Start");

                }

                //linearLayoutRhythm.removeAllViews();

                //playSound();
            }
        });

//        buttonStop1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                if (!play) return;
//                StopMetronomeThread();
//
//                StartWrite();
//
//            }
//        });

//        buttonPDF.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                PdfTest();
//
//            }
//        });

        //Toast.makeText(getApplicationContext(), (String) ("Play the rhythm by touching the red surface.\nNotation will be shown after the metronome is stopped."), Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), (String) (getResources().getString(R.string.write_description)), Toast.LENGTH_LONG).show();


    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (metronomeThreadWrite == null) return;
        play = false;
        metronomeThreadWrite.endLooper();
        try
        {
            metronomeThreadWrite.join();
        }catch(InterruptedException e) {}

        metronomeThreadWrite = null;

    }

    void StartMetronomeThread()
    {
        if (metronomeThreadWrite != null) return;
        metronomeThreadWrite = new MetronomeThreadWrite(writeActivity);
        metronomeThreadWrite.start();
    }

    void StopMetronomeThread()
    {
        if (metronomeThreadWrite == null) return;
        play = false;

        try
        {
            metronomeThreadWrite.join();
        }catch(InterruptedException e) {}


        metronomeThreadWrite = null;
    }

    private void StartWrite()
    {
        if (ClickTimeList.size() == 0 || metronomeTimeList.size() == 0) return;

        CalculateDurations();
        int upperDuration = 0, barDuration, barSoFar = 0;
        switch (lower)
        {
            case 1:
                upperDuration = (int)_1note;
                break;
            case 2:
                upperDuration = (int)_2note;
                break;
            case 4:
                upperDuration = (int)_4note;
                break;
            case 8:
                upperDuration = (int)_8note;
                break;
            case 16:
                upperDuration = (int)_16note;
                break;
        }
        barDuration = upperDuration * upper;

        IntObj durationObj = new IntObj();
        rhythm = "|";

        long noteDuration = 0;
        int j = 0;
        noteDuration = (long) Math.round((float) (( (long) ClickTimeList.get(0) / 1000000 - (long) metronomeTimeList.get(0) / 1000000 )));
        for (j = 0; j < metronomeTimeList.size(); j+=upper)
        {

            if ( (long) metronomeTimeList.get(j) > (long) ClickTimeList.get(0) )
            {
                break;
            }
        }


        //BarBeginning();


        boolean newBar = false;
        int nextBar = j; // indeks udarca metronoma koji oznacava pocetak sljedeceg takta


        // trajanje od pocetka takta do prve note
        noteDuration = (long) Math.round((float) (( (long) ClickTimeList.get(0) / 1000000 - (long) metronomeTimeList.get(j-upper) / 1000000 )));

        //note = Write3(noteDuration, 'r', durationObj);

        note = "";
        if ( noteDuration <= (barDuration - notesList.get(shortestNote)/2.0f ))
        {
            //note = Write3(noteDuration, 'r', durationObj);
            note = StartWrite4(noteDuration, 'r', durationObj);
            //nextBar += upper;
        }
        else
        {
            ClickTimeList.set(0, metronomeTimeList.get(j));
            nextBar += upper;
        }




        rhythm += note;
        barSoFar += durationObj.value;

        //if (note.equals("") == false) NoteEnd();

        durationsList = new ArrayList();

        long error = noteDuration - durationObj.value;

        for (int i = 1; i < ClickTimeList.size(); i++)
        {

            noteDuration =  (long) Math.round((float) (( (long) ClickTimeList.get(i) / 1000000 - (long) ClickTimeList.get(i-1) / 1000000 ) /*/ 10000.0*/ )); // [ms]

            // klik - (pocetak takta + barSoFar) ?
            noteDuration = (long) Math.round( ((long) ClickTimeList.get(i)/ 1000000 - ( (long) metronomeTimeList.get(nextBar - upper)/ 1000000 + barSoFar )) );

            // korekcija sljedece note zbog greske?
            //noteDuration += error; // <--- ?

            durationsList.add(noteDuration);
            note = "";

            // while ?
            if ( nextBar < metronomeTimeList.size() && (long) ClickTimeList.get(i) > (long) metronomeTimeList.get(nextBar) )
            {

                long firstPart, // dio note do kraja prvog takta
                        secondPart; // dio note od pocetka drugog takta

                //firstPart = (long) Math.round((float) (( (long) metronomeTimeList.get(nextBar) / 1000000 - (long) ClickTimeList.get(i - 1) / 1000000 ) ));
                firstPart = barDuration - barSoFar;




                //note = Write3((long) (firstPart), 'n', durationObj);
                note = StartWrite4((long) (firstPart), 'n', durationObj);
                rhythm += note;
                rhythm += "|";

                while ( (nextBar + upper)<metronomeTimeList.size() && (long) ClickTimeList.get(i) > (long) metronomeTimeList.get(nextBar + upper) )
                {
                    nextBar += upper;
                    note = StartWrite4((long) (barDuration), 'r', durationObj);
                    rhythm += note;
                    rhythm += "|";
                }

                secondPart = (long) Math.round((float) (( (long) ClickTimeList.get(i) / 1000000 - (long) metronomeTimeList.get(nextBar) / 1000000 ) ));

                //if (secondPart < notesList.get(shortestNote)) NoteEnd();


                //BarBeginning();

                if (firstPart < notesList.get(shortestNote))
                    //note = Write3((long) (secondPart), 'n', durationObj);
                    note = StartWrite4((long) (secondPart), 'n', durationObj);
                else
                    //note = Write3((long) (secondPart), 'r', durationObj);
                    note = StartWrite4((long) (secondPart), 'r', durationObj);


                rhythm += note;
                barSoFar = (int) secondPart;

                //if (secondPart >= notesList.get(shortestNote)) NoteEnd();

                nextBar += upper;
            }
            else
            {
                /*
            imageViewNote = new ImageView(this);
            imageViewNote.setScaleType(ImageView.ScaleType.FIT_XY);
            imageViewNote.setLayoutParams(new ViewGroup.LayoutParams(height/3, height));
            */

                //note = Write2( (long) (noteDuration) );
                while (noteDuration >  notesList.get(notesList.size()-1))
                {
                    noteDuration -= notesList.get(notesList.size()-1);
                    rhythm += "w-";
                }


                //note = Write3((long) (noteDuration), 'n', durationObj);
                note = StartWrite4((long) (noteDuration), 'n', durationObj);

                // greska?
                error = noteDuration - durationObj.value;

                rhythm += note;
                barSoFar += durationObj.value;

                if (Math.abs(barSoFar - barDuration) < 1)
                {
                    nextBar += upper;
                    barSoFar = 0;
                    rhythm += "|";
                }

                // oznaci kraj note
                //NoteEnd();
            }


            //if ( i != (long) ClickTimeList.size() - 1 ) rhythm += ", ";
            if ( i == (long) ClickTimeList.size() - 1 ) // dodaj cetvrtinku na kraj
            {
                //rhythm += ", " + note;

                noteDuration = barDuration - barSoFar;

                if (noteDuration < notesList.get(shortestNote))
                {
                    rhythm += "|";
                    noteDuration = barDuration;
                }


                note = StartWrite4((long) (noteDuration), 'n', durationObj);
                //note = Write3((long) (noteDuration), 'n', durationObj);


                //rhythm += "e";
                rhythm += note + "|";


            }


        }

        //rhythm += "|";

        //rhythm = RearrangeRhythm(rhythm);

        textViewNotation.setText(rhythm);
    }

    String RearrangeRhythm (String rhythm)
    {
        char tempChar = 0;
        char lastNote = 0, nextNote = 0;
        String rhythmNew = "";
        int number8 = 0, number16 = 0;

        for (int i=0; i<rhythm.length(); i++)
        {
            if (i<rhythm.length()-1)
                nextNote = rhythm.charAt(i+1);

            tempChar = rhythm.charAt(i);
            switch (tempChar)
            {
                case 'e':
                    number8++;
                    number16 = 0;

                    if (lastNote != 'e')
                    {
                        if (nextNote == 'e')
                            rhythmNew += 'r';
                        else
                            rhythmNew += 'e';
                    }
                    else
                    {
                        if (nextNote == 'e')
                        {
                            if (number8 %2 == 1)
                                rhythmNew += 'r';

                            else
                                rhythmNew += 'y';
                        }

                        else
                        {
                            if (number8 %2 == 1)
                                rhythmNew += 'y';
                            else
                                rhythmNew += 'e';
                        }


//                        if (nextNote == 'e')
//                            rhythmNew += 't';
//                        else rhythmNew += 'y';
                    }

                    lastNote = 'e';
                    break;

                case 's':
                    number16++;
                    number8 = 0;

                    if (lastNote != 's')
                    {
                        if (nextNote == 's')
                            rhythmNew += 'd';
                        else
                            rhythmNew += 's';
                    }
                    else
                    {
                        if (nextNote == 's')
                        {
                            if (number16 % 4 == 1)
                                rhythmNew += 'g';

                            else
                                rhythmNew += 'f';
                        }

                        else rhythmNew += 'g';

//                        if (nextNote == 'e')
//                            rhythmNew += 't';
//                        else rhythmNew += 'y';
                    }

                    lastNote = 'e';
                    break;

                default:
                    number16 = 0;
                    number8 = 0;
                    lastNote = 0;
                    rhythmNew += tempChar;
            }

        }

        return rhythmNew;
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

    String WriteNote(List<String> temp)
    {
        int lastNote = -1, nextNote = -1;

        if (temp.get(0).equals("")) return "";

        String noteFont = "";

        for (int j = 0; j < temp.size(); j++ )
        {


            switch (Integer.parseInt(temp.get(j))) {
                case 0:

                    noteFont += "z";
                    break;
                /*case 1:
                    imageViewNote.setImageResource (R.drawable.lower_16th_note);
                    break;*/
                case 1:

                    noteFont += "s";
                    lastNote = 1;
                    break;
                case 2:

                    noteFont += "Oe";
                    lastNote = 2;
                    break;
                case 3:

                    noteFont += "s.";
                    lastNote = 3;
                    break;
                case 4:

                    noteFont += "e";
                    break;
                case 5:

                    noteFont += "Oq";
                    lastNote = 5;
                    break;
                case 6:

                    noteFont += "e.";
                    lastNote = 6;
                    break;
                case 7:

                    noteFont += "q";
                    lastNote = 7;
                    break;
                case 8:

                    noteFont += "Oh";
                    lastNote = 8;
                    break;
                case 9:

                    noteFont += "q.";
                    lastNote = 9;
                    break;
                case 10:

                    noteFont += "h";
                    lastNote = 10;
                    break;
                case 11:

                    noteFont += "Ow";
                    lastNote = 11;
                    break;
                case 12:

                    noteFont += "h.";
                    lastNote = 12;
                    break;
                case 13:

                    noteFont += "w";
                    lastNote = 13;
                    break;
            }

            if (j != temp.size() - 1) noteFont += "-";
            //linearLayoutRhythm.addView(imageViewNote);

        }

        return noteFont;
    }

    String WriteRest3(List<String> temp)
    {
        if (temp.get(0).equals("")) return "";

        String noteFont = "";

        for (int j = 0; j < temp.size(); j++ )
        {


            switch (Integer.parseInt(temp.get(j))) {
                case 0:

                    noteFont += "Z";
                    break;
                /*case 1:
                    imageViewNote.setImageResource (R.drawable.lower_16th_note);
                    break;*/
                case 1:

                    noteFont += "S";
                    break;
                case 2:

                    noteFont += "OE";
                    break;
                case 3:

                    noteFont += "S.";
                    break;
                case 4:

                    noteFont += "E";
                    break;
                case 5:

                    noteFont += "OQ";
                    break;
                case 6:

                    noteFont += "E.";
                    break;
                case 7:

                    noteFont += "Q";
                    break;
                case 8:

                    noteFont += "OH";
                    break;
                case 9:

                    noteFont += "Q.";
                    break;
                case 10:

                    noteFont += "H";
                    break;
                case 11:

                    noteFont += "OW";
                    break;
                case 12:

                    noteFont += "H.";
                    break;
                case 13:

                    noteFont += "W";
                    break;
            }

            //linearLayoutRhythm.addView(imageViewNote);

        }

        return noteFont;
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

    String Write3 (long noteDuration, char noteOrRest, IntObj durationObj)
    {
        int shortestNoteTemp = shortestNote;
        //if (noteDuration < notesList.get(shortestNote)) return "";

        while (     noteDuration < notesList.get(shortestNoteTemp)  // ako je trajanje krace od najkrace note
                &&  shortestNoteTemp > 0)                           // smanji najkracu
        {
            shortestNoteTemp--;
        }

        if ( noteDuration < notesList.get(0) )
        {
//            if (noteOrRest == 'n')
//            {
//                List<String> temp = Arrays.asList("0");
//                WriteNote(temp);
//                return ("0");
//            }
//            else return ("");

            return ("");
        }

        float duration = 0, durationT = 0, durationT1 = 0;
        int i = 0, numNotes = 0, numNotesT = 0;
        String note = "", noteT = "";
        String returnString;

        while ( notesList.get(shortestNoteTemp) <= noteDuration - duration )
        {
            //for (i = 0; i < notesList.size(); i++)
            for (i = shortestNoteTemp; i < notesList.size(); i++) //<---
            {
                if ( i == 3) continue;

                if (duration + notesList.get(i) > noteDuration)
                    break;
            }
            if (i == 4) i = 3;

            duration += notesList.get(i - 1);
            note += i - 1 + ",";
        }

        //nakon prvog zapisivanja jednostavno zamijeni najkracu notu onom koja najbolje pase

        List<String> temp;
        temp = (Arrays.asList(note.split(",")));

        //----------------------------

        float greska = Math.abs(duration - noteDuration);
        float bestDuration = duration;

        //----------------------------


//        List<Integer> triplets = Arrays.asList(2, 5, 8, 11);
//        String bestNote = note;
//        ArrayList<String> sameNotes = new ArrayList<String>();
//
//
//        for (int k = 0; k<triplets.size(); k++)
//        {
//            if ( notesList.get(triplets.get(k)) > noteDuration ) continue;
//
//            note = Integer.toString(triplets.get(k)) + ",";
//            duration = notesList.get(triplets.get(k));
//
//            while ( notesList.get(shortestNoteTemp) <= noteDuration - duration )
//            {
//                for (i = shortestNoteTemp; i < notesList.size(); i++)
//                {
//                    if ( triplets.contains( i ) ) continue;
//
//                    if (duration + notesList.get(i) > noteDuration)
//                        break;
//                }
//                duration += notesList.get(i - 1);
//                note += i - 1 + ",";
//            }
//
//            temp = (Arrays.asList(note.split(",")));
//            int lastNote = Integer.parseInt(temp.get(temp.size() - 1));
//            float bestDurationLastNote = duration;
//
//
//            for (int j = shortestNoteTemp; j<notesList.size(); j++)
//            {
//                if ( Math.abs( noteDuration - ( duration - notesList.get(lastNote) + notesList.get(j) ) )
//                        < Math.abs( noteDuration - bestDurationLastNote) - 1 )
//                {
//                    bestDurationLastNote = duration - notesList.get(lastNote) + notesList.get(j);
//                    temp.set(temp.size()-1, Integer.toString(j));
//
//                    note = "";
//                    for (int l = 0; l<temp.size(); l++)
//                    {
//                        note += temp.get(l) + ",";
//                    }
//                }
//            }
//            duration = bestDurationLastNote;
//            bestNote = note;
//
//            if (Math.abs(Math.abs( noteDuration - duration ) - Math.abs( noteDuration - bestDuration )) < 1 )
//            {
//                sameNotes.add(note);
//
//            }
//            else if ( Math.abs( noteDuration - duration ) < Math.abs( noteDuration - bestDuration ) )
//            {
//                bestDuration = duration;
//                bestNote = note;
//                sameNotes = new ArrayList<String>();
//                sameNotes.add(note);
//            }
//
//        }

        //----------------------------

        //float shortestNote = notesList.get(  Integer.parseInt(temp.get(temp.size()-1)) );

        for (int j = shortestNoteTemp; j<notesList.size(); j++)
        {
            if ( j == 3) continue;

            if ( Math.abs( noteDuration - ( duration - notesList.get(i-1) + notesList.get(j) ) ) < greska )
            {
                bestDuration = duration - notesList.get(i-1) + notesList.get(j);
                greska = Math.abs(noteDuration - bestDuration);
            }
        }

        durationObj.value = (int) bestDuration;
        returnString = WriteNoteSimple(bestDuration, shortestNoteTemp);

        //----------------------------

        temp = (Arrays.asList(returnString.split(",")));



        //temp = (Arrays.asList(bestNote.split(",")));

//        int numTriplets = 0;
//        int minTriplets = 0;
//        List<String> leastTripletedNote = temp;
//        for (int k = 0; k < temp.size(); k++)
//        {
//            if (triplets.contains(Integer.parseInt(temp.get(k)))) minTriplets++;
//        }
//        for (int k = 0; k < sameNotes.size(); k++)
//        {
//            numTriplets = 0;
//
//            List<String> tempSameNote = (Arrays.asList(sameNotes.get(k).split(",")));
//
//
//            for (int l = 0; l < temp.size(); l++)
//            {
//                if (triplets.contains(Integer.parseInt(tempSameNote.get(l)))) numTriplets++;
//            }
//            if (numTriplets<minTriplets)
//            {
//                leastTripletedNote = tempSameNote;
//                minTriplets = numTriplets;
//            }
//        }
//
//        temp = leastTripletedNote;

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
        if (noteOrRest == 'n')
            returnString = WriteNote(temp);
        else
            returnString = WriteRest3(temp);

        return returnString;
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

    String StartWrite4 ( float duration, char noteOrRest, IntObj durationObj )
    {
//        if (/*noteOrRest == 'r' &&*/ duration < notesList.get(shortestNote)/2.0f)
//        {
//            durationObj.value = 0;
//            return "";
//        }
//        else if (duration < notesList.get(shortestNote))
//        {
//            durationObj.value = Math.round( (notesList.get(shortestNote)));
//            if (noteOrRest == 'n')
//                return "s";
//            else
//                return "S";
//        }

        Note note = Write4 (duration, new ArrayList());
        if (note.duration==0)
        {
            durationObj.value = 0;
            return "";
        }


        durationObj.value = (int) note.duration;
        List<String> noteSplit = (Arrays.asList(note.notation.split(",")));
        List<Integer> noteSplitInt = new ArrayList<>();
        for(String s : noteSplit) noteSplitInt.add(Integer.valueOf(s));
        Collections.sort(noteSplitInt);
        Collections.reverse(noteSplitInt);
        noteSplit = new ArrayList<>();
        for(int i : noteSplitInt) noteSplit.add(Integer.toString(i));

        String returnString;
        if (noteOrRest == 'n')
            returnString = WriteNote(noteSplit);
        else
            returnString = WriteRest3(noteSplit);
        return returnString;
    }

    void PdfTest()
    {
        // Create a shiny new (but blank) PDF document in memory
        // We want it to optionally be printable, so add PrintAttributes
        // and use a PrintedPdfDocument. Simpler: new PdfDocument().
//        PrintAttributes printAttrs = new PrintAttributes.Builder().
//                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
//                setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
//                setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, 300, 300)).
//                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
//                build();

        PrintAttributes printAttrs = new PrintAttributes.Builder().build();

        PdfDocument document = new PrintedPdfDocument(this, printAttrs);
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 300, 1).create();
        // create a new page from the PageInfo
        PdfDocument.Page page = document.startPage(pageInfo);
        // repaint the user's text into the page
        View content = findViewById(R.id.textViewNotation);
        content.draw(page.getCanvas());
        // do final processing of the page
        document.finishPage(page);
        // Here you could add more pages in a longer doc app, but you'd have
        // to handle page-breaking yourself in e.g., write your own word processor...
        // Now write the PDF document to a file; it actually needs to be a file
        // since the Share mechanism can't accept a byte[]. though it can
        // accept a String/CharSequence. Meh.
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/pruebaAppModerator.pdf");
            FileOutputStream fos = new FileOutputStream(f);
            document.writeTo(fos);
            document.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating file", e);
        }
    }




}

class MetronomeThreadWrite extends Thread {

    WriteActivity writeActivity;

    private volatile Looper looper;
    public int counter;

    public MetronomeThreadWrite(WriteActivity pwriteActivity)
    {
        writeActivity = pwriteActivity;
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
        int counter = writeActivity.upper;
        writeActivity.play = true;

        long start, start1, delay, end;

        while (writeActivity.play)
        {
            //mainActivity.playSound();

            start = System.nanoTime();

            if (counter == writeActivity.upper)
            {
                writeActivity.metronomeTimeList.add(System.nanoTime() + 1000000 * writeActivity.metronomeDelayCorrection);
                writeActivity.soundPool.play(writeActivity.soundIDLow, 1, 1, 1, 0, 1f);


                //mainActivity.playClickLow();
                //mainActivity.playClickHigh();

                counter = 1;
            }
            else
            {
                writeActivity.metronomeTimeList.add(System.nanoTime());
                writeActivity.soundPool.play(writeActivity.soundIDHigh, 1, 1, 1, 0, 1f);


                //mainActivity.playClickHigh();


                counter++;
            }

            delay = System.nanoTime() - start;
            start1 = System.nanoTime();
            end = start1 + writeActivity.bpmSleep * 1000000 - (delay);
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
